package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class MultiGraphInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    @Test
    public void testMultipleEdgesBetweenSameNodes() {
        Node a = factory.createNode();
        Node b = factory.createNode();
        Edge e1 = factory.createEdge(a, b);
        e1.tags().add("type1");
        Edge e2 = factory.createEdge(a, b);
        e2.tags().add("type2");

        Graph graph = factory.createGraph(new EphemeralNodeSet(a, b));
        graph.addEdge(e1);
        graph.addEdge(e2);

        Graph result = graph.forwardStep(a);
        assertEquals(2, result.nodes().size());
        assertEquals(2, result.edges().size(), "Should include both edges between the same nodes");
        assertTrue(result.edges().contains(e1));
        assertTrue(result.edges().contains(e2));
    }
}
