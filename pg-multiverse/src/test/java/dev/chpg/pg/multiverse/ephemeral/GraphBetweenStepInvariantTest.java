package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphBetweenStepInvariantTest {
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
    public void testBetweenStepIsIntersectionOfForwardStepAndReverseStep() {
        Graph forwardStepA = graph.forwardStep(a);
        Graph reverseStepB = graph.reverseStep(b);
        Graph betweenStepAB = graph.betweenStep(a, b);

        Graph intersection = forwardStepA.intersection(reverseStepB);
        assertGraphsEqual(intersection, betweenStepAB);
    }
}
