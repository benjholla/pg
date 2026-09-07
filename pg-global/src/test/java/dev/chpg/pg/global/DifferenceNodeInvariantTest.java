package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class DifferenceNodeInvariantTest {

    @Test
    public void testDifferenceNodeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);
        Edge e2 = factory.createEdge(n2, n3);

        Graph g = factory.createGraph();
        g.addEdge(e1);
        g.addEdge(e2);

        // Subtract n2, which is incident to both edges
        Graph diff = g.difference(n2);

        assertFalse(diff.nodes().contains(n2));
        assertTrue(diff.nodes().contains(n1));
        assertTrue(diff.nodes().contains(n3));
        assertEquals(2, diff.nodes().size());

        // Inherently cascades to remove all incident edges
        assertFalse(diff.edges().contains(e1));
        assertFalse(diff.edges().contains(e2));
        assertEquals(0, diff.edges().size());

        // Subtract node not in graph
        Node n4 = factory.createNode();
        Graph diff2 = g.difference(n4);
        assertEquals(3, diff2.nodes().size());
        assertEquals(2, diff2.edges().size());
    }
}
