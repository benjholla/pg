package io.github.benjholla.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MultiGraphInvariantTest {
    @Test
    public void testMultipleEdgesBetweenSameNodes() {
        Node a = new HeavyNode();
        Node b = new HeavyNode();
        Edge e1 = new HeavyEdge(a, b);
        e1.tags().add("type1");
        Edge e2 = new HeavyEdge(a, b);
        e2.tags().add("type2");

        HeavyGraph graph = new HeavyGraph(a, b);
        graph.add(e1);
        graph.add(e2);

        Graph result = graph.forwardStep(a);
        assertEquals(2, result.nodes().size());
        assertEquals(2, result.edges().size(), "Should include both edges between the same nodes");
        assertTrue(result.edges().contains(e1));
        assertTrue(result.edges().contains(e2));
    }
}
