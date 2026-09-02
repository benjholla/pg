package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphTraversalInvariantTest {
    private static final Universe universe = new Universe();
    private static final EphemeralFactory factory = new EphemeralGraph(universe).factory();

    private Graph graph;
    private Node a, b, c, d, e, f;

    @BeforeEach
    public void setUp() {
        graph = factory.createGraph();
        a = factory.createNode(); b = factory.createNode(); c = factory.createNode();
        d = factory.createNode(); e = factory.createNode(); f = factory.createNode();

        graph.addEdge(factory.createEdge(a, b));
        graph.addEdge(factory.createEdge(b, c));
        graph.addEdge(factory.createEdge(c, d));
        graph.addEdge(factory.createEdge(d, b)); // cycle b-c-d-b
        graph.addEdge(factory.createEdge(e, f));
    }

    private void assertGraphsEqual(Graph expected, Graph actual) {
        assertEquals(expected.nodes().size(), actual.nodes().size(), "Node count mismatch");
        assertEquals(expected.edges().size(), actual.edges().size(), "Edge count mismatch");
        assertTrue(expected.nodes().containsAll(actual.nodes()), "Nodes mismatch");
        assertTrue(actual.nodes().containsAll(expected.nodes()), "Nodes mismatch");
        assertTrue(expected.edges().containsAll(actual.edges()), "Edges mismatch");
        assertTrue(actual.edges().containsAll(expected.edges()), "Edges mismatch");
    }

    @Test
    public void testBetweenIsIntersectionOfForwardAndReverse() {
        Graph forwardA = graph.forward(a);
        Graph reverseD = graph.reverse(d);
        Graph betweenAD = graph.between(a, d);

        Graph intersection = forwardA.intersection(reverseD);
        assertGraphsEqual(intersection, betweenAD);
    }

    @Test
    public void testTraversalOverloadEquivalence() {
        Graph graphA = factory.createGraph(new EphemeralNodeSet(a));
        Graph graphD = factory.createGraph(new EphemeralNodeSet(d));

        // forward()
        assertGraphsEqual(graph.forward(graphA.nodes()), graph.forward(a));
        assertGraphsEqual(graph.forward(graphA.nodes()), graph.forward(graphA));

        // reverse()
        assertGraphsEqual(graph.reverse(graphD.nodes()), graph.reverse(d));
        assertGraphsEqual(graph.reverse(graphD.nodes()), graph.reverse(graphD));

        // between()
        assertGraphsEqual(graph.between(graphA.nodes(), graphD.nodes()), graph.between(a, d));
        assertGraphsEqual(graph.between(graphA.nodes(), graphD.nodes()), graph.between(graphA, graphD));

        // forwardStep()
        assertGraphsEqual(graph.forwardStep(graphA.nodes()), graph.forwardStep(a));
        assertGraphsEqual(graph.forwardStep(graphA.nodes()), graph.forwardStep(graphA));

        // reverseStep()
        assertGraphsEqual(graph.reverseStep(graphD.nodes()), graph.reverseStep(d));
        assertGraphsEqual(graph.reverseStep(graphD.nodes()), graph.reverseStep(graphD));

        // betweenStep()
        assertGraphsEqual(graph.betweenStep(graphA.nodes(), graphD.nodes()), graph.betweenStep(a, d));
        assertGraphsEqual(graph.betweenStep(graphA.nodes(), graphD.nodes()), graph.betweenStep(graphA, graphD));
    }

    @Test
    public void testForwardUnionDistributiveProperty() {
        // forward(A U E) == forward(A) U forward(E)
        Graph unionNodes = factory.createGraph(new EphemeralNodeSet(a, e));
        Graph forwardUnion = graph.forward(unionNodes);

        Graph forwardA = graph.forward(a);
        Graph forwardE = graph.forward(e);
        Graph unionOfForwards = forwardA.union(forwardE);

        assertGraphsEqual(unionOfForwards, forwardUnion);
    }
}
