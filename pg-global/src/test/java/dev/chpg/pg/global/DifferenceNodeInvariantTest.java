package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DifferenceNodeInvariantTest {

    @Test
    public void testDifferenceNodeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph diff0 = g0.difference(n1);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        // Case 2: graph has only the node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        Graph diff1 = g1.difference(n1);
        assertEquals(0, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        // Case 3: graph has the node, but differentiating with another node not in the graph
        Graph diff1_n2 = g1.difference(n2);
        assertTrue(diff1_n2.nodes().contains(n1));
        assertEquals(1, diff1_n2.nodes().size());
        assertEquals(0, diff1_n2.edges().size());

        // Case 4: graph has both nodes and an edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e);

        Graph diff3 = g3.difference(n1);
        assertFalse(diff3.nodes().contains(n1));
        assertTrue(diff3.nodes().contains(n2));
        assertFalse(diff3.edges().contains(e)); // edge must cascade delete
        assertEquals(1, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());
    }
}
