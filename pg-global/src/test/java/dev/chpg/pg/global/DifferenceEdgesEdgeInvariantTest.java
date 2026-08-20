package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DifferenceEdgesEdgeInvariantTest {

    @Test
    public void testDifferenceEdgesEdgeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e12 = factory.createEdge(n1, n2);
        Edge e23 = factory.createEdge(n2, n3);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph diff0 = g0.differenceEdges(e12);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        // Case 2: graph has only the terminal nodes, but not the edge
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        g1.addNode(n2);
        Graph diff1 = g1.differenceEdges(e12);
        // differenceEdges(Edge) should NOT subtract the terminal nodes.
        assertEquals(2, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());
        assertTrue(diff1.nodes().contains(n1));
        assertTrue(diff1.nodes().contains(n2));

        // Case 3: graph has the edge
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        g2.addEdge(e12);
        Graph diff2 = g2.differenceEdges(e12);
        assertEquals(2, diff2.nodes().size());
        assertEquals(0, diff2.edges().size());
        assertTrue(diff2.nodes().contains(n1));
        assertTrue(diff2.nodes().contains(n2));

        // Case 4: graph has other nodes and edges
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addNode(n3);
        g3.addEdge(e12);
        g3.addEdge(e23);

        Graph diff3 = g3.differenceEdges(e12);
        // subtracting e12 removes e12 but keeps all nodes. e23 is also kept.
        assertEquals(3, diff3.nodes().size());
        assertEquals(1, diff3.edges().size());
        assertTrue(diff3.edges().contains(e23));
        assertFalse(diff3.edges().contains(e12));
    }
}
