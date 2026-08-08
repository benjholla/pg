package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphBetweenStepInvariantTest {
    private GlobalGraph graph;
    private Node a, b, c, d, e, f;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        a = new GlobalNode(); b = new GlobalNode(); c = new GlobalNode();
        d = new GlobalNode(); e = new GlobalNode(); f = new GlobalNode();

        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(c, d));
        graph.addEdge(new GlobalEdge(d, b)); // cycle b-c-d-b
        graph.addEdge(new GlobalEdge(e, f));
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
