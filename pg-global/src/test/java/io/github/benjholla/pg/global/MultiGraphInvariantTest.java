package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class MultiGraphInvariantTest {
    @Test
    public void testMultipleEdgesBetweenSameNodes() {
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        Edge e1 = new GlobalEdge(a, b);
        e1.tags().add("type1");
        Edge e2 = new GlobalEdge(a, b);
        e2.tags().add("type2");

        GlobalGraph graph = new GlobalGraph(a, b);
        graph.addEdge(e1);
        graph.addEdge(e2);

        Graph result = graph.forwardStep(a);
        assertEquals(2, result.nodes().size());
        assertEquals(2, result.edges().size(), "Should include both edges between the same nodes");
        assertTrue(result.edges().contains(e1));
        assertTrue(result.edges().contains(e2));
    }
}
