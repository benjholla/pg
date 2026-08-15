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
        Edge e12 = factory.createEdge(n1, n2);
        Edge e23 = factory.createEdge(n2, n3);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph diff0 = g0.difference(n1);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        // Case 2: graph does not contain the node
        Graph g1 = factory.createGraph();
        g1.addNode(n2);
        Graph diff1 = g1.difference(n1);
        assertEquals(1, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());
        assertTrue(diff1.nodes().contains(n2));

        // Case 3: graph contains the node, no edges
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        Graph diff2 = g2.difference(n1);
        assertEquals(1, diff2.nodes().size());
        assertEquals(0, diff2.edges().size());
        assertTrue(diff2.nodes().contains(n2));
        assertFalse(diff2.nodes().contains(n1));

        // Case 4: graph contains the node and incident edges, cascade removal
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addNode(n3);
        g3.addEdge(e12);
        g3.addEdge(e23);

        Graph diff3 = g3.difference(n2);

        // Subtracting n2 should cascade and remove e12 and e23, but leave n1 and n3
        assertEquals(2, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());
        assertTrue(diff3.nodes().contains(n1));
        assertTrue(diff3.nodes().contains(n3));
        assertFalse(diff3.nodes().contains(n2));

        // Case 5: Subtracting a node should not disturb other edges
        Graph diff4 = g3.difference(n1);
        // Subtracting n1 should remove e12, leave n2, n3 and e23
        assertEquals(2, diff4.nodes().size());
        assertEquals(1, diff4.edges().size());
        assertTrue(diff4.nodes().contains(n2));
        assertTrue(diff4.nodes().contains(n3));
        assertFalse(diff4.nodes().contains(n1));
        assertTrue(diff4.edges().contains(e23));
        assertFalse(diff4.edges().contains(e12));
    }
}
