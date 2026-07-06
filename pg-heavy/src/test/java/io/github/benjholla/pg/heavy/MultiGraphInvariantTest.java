package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class MultiGraphInvariantTest {
    @Test
    public void testMultipleEdgesBetweenSameNodes() {
        Node a = (HeavyNode) new HeavyGraph().createNode();
        Node b = (HeavyNode) new HeavyGraph().createNode();
        Edge e1 = (HeavyEdge) new HeavyGraph().createEdge(a, b);
        e1.tags().add("type1");
        Edge e2 = (HeavyEdge) new HeavyGraph().createEdge(a, b);
        e2.tags().add("type2");

        HeavyGraph graph = (HeavyGraph) new HeavyGraph().createGraph(a, b);
        graph.addEdge(e1);
        graph.addEdge(e2);

        Graph result = graph.forwardStep(a);
        assertEquals(2, result.nodes().size());
        assertEquals(2, result.edges().size(), "Should include both edges between the same nodes");
        assertTrue(result.edges().contains(e1));
        assertTrue(result.edges().contains(e2));
    }
}
