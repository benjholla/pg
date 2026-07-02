package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class GraphTraversalInvariantTest {
    private HeavyGraph graph;
    private Node a, b, c, d, e, f;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        a = new HeavyNode(); b = new HeavyNode(); c = new HeavyNode();
        d = new HeavyNode(); e = new HeavyNode(); f = new HeavyNode();

        graph.add(new HeavyEdge(a, b));
        graph.add(new HeavyEdge(b, c));
        graph.add(new HeavyEdge(c, d));
        graph.add(new HeavyEdge(d, b)); // cycle b-c-d-b
        graph.add(new HeavyEdge(e, f));
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
    public void testForwardUnionDistributiveProperty() {
        // forward(A U E) == forward(A) U forward(E)
        Graph unionNodes = new HeavyGraph(a, e);
        Graph forwardUnion = graph.forward(unionNodes);

        Graph forwardA = graph.forward(a);
        Graph forwardE = graph.forward(e);
        Graph unionOfForwards = forwardA.union(forwardE);

        assertGraphsEqual(unionOfForwards, forwardUnion);
    }
}
