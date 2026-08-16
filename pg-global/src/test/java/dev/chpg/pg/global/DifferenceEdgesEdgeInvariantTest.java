package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DifferenceEdgesEdgeInvariantTest {

    @Test
    public void testDifferenceEdgesEdgeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph diff0 = g0.differenceEdges(e);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        // Case 2: graph has nodes, but not the edge
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        g1.addNode(n2);
        Graph diff1 = g1.differenceEdges(e);
        assertTrue(diff1.nodes().contains(n1));
        assertTrue(diff1.nodes().contains(n2));
        assertEquals(2, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        // Case 3: graph has the edge, but differing with another edge not in the graph
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        g2.addEdge(e);
        Edge e2 = factory.createEdge(n1, n2);
        Graph diff2 = g2.differenceEdges(e2);
        assertTrue(diff2.nodes().contains(n1));
        assertTrue(diff2.nodes().contains(n2));
        assertTrue(diff2.edges().contains(e));
        assertEquals(2, diff2.nodes().size());
        assertEquals(1, diff2.edges().size());

        // Case 4: graph has the edge, differing it out
        Graph diff3 = g2.differenceEdges(e);
        assertTrue(diff3.nodes().contains(n1)); // nodes are retained
        assertTrue(diff3.nodes().contains(n2));
        assertFalse(diff3.edges().contains(e)); // edge is removed
        assertEquals(2, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());
    }
}
