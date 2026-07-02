package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraphTransitivityInvariantTest {
    private HeavyGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        a = new HeavyNode(); b = new HeavyNode(); c = new HeavyNode();
        d = new HeavyNode(); e = new HeavyNode();

        graph.add(new HeavyEdge(a, b));
        graph.add(new HeavyEdge(b, c));
        graph.add(new HeavyEdge(c, d));
        graph.add(new HeavyEdge(d, e));
        graph.add(new HeavyEdge(c, a)); // create a cycle to make things interesting
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
    public void testForwardIsIdempotent() {
        Graph forwardA = graph.forward(a);
        Graph forwardForwardA = graph.forward(forwardA);
        assertGraphsEqual(forwardA, forwardForwardA);
    }

    @Test
    public void testReverseIsIdempotent() {
        Graph reverseE = graph.reverse(e);
        Graph reverseReverseE = graph.reverse(reverseE);
        assertGraphsEqual(reverseE, reverseReverseE);
    }

    @Test
    public void testForwardContainsForwardStep() {
        Graph forwardA = graph.forward(a);
        Graph forwardStepA = graph.forwardStep(a);

        // forwardStep(A) should be a subgraph of forward(A)
        assertTrue(forwardA.nodes().containsAll(forwardStepA.nodes()), "forward(a) should contain all nodes of forwardStep(a)");
        assertTrue(forwardA.edges().containsAll(forwardStepA.edges()), "forward(a) should contain all edges of forwardStep(a)");
    }

    @Test
    public void testBetweenSubsets() {
        Graph betweenAE = graph.between(a, e);
        Graph forwardA = graph.forward(a);
        Graph reverseE = graph.reverse(e);

        // between(A, E) must be a subgraph of both forward(A) and reverse(E)
        assertTrue(forwardA.nodes().containsAll(betweenAE.nodes()));
        assertTrue(forwardA.edges().containsAll(betweenAE.edges()));

        assertTrue(reverseE.nodes().containsAll(betweenAE.nodes()));
        assertTrue(reverseE.edges().containsAll(betweenAE.edges()));
    }
}
