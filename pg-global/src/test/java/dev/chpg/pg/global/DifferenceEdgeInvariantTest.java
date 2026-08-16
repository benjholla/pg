package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DifferenceEdgeInvariantTest {

    @Test
    public void testDifferenceEdgeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph diff0 = g0.difference(e);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        // Case 2: graph has only nodes, not the edge
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        g1.addNode(n2);
        Graph diff1 = g1.difference(e);
        assertFalse(diff1.nodes().contains(n1)); // edge removes nodes
        assertFalse(diff1.nodes().contains(n2));
        assertEquals(0, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        // Case 3: graph has the edge, but differing with another edge not in the graph
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        g2.addEdge(e);
        Edge e2 = factory.createEdge(n1, n2); // different edge object
        Graph diff2 = g2.difference(e2);
        assertFalse(diff2.nodes().contains(n1));
        assertFalse(diff2.nodes().contains(n2));
        assertFalse(diff2.edges().contains(e));
        assertEquals(0, diff2.nodes().size());
        assertEquals(0, diff2.edges().size());

        // Case 4: graph has the edge
        Graph diff3 = g2.difference(e);
        assertFalse(diff3.nodes().contains(n1)); // removing an edge implicitly removes its terminal nodes
        assertFalse(diff3.nodes().contains(n2));
        assertFalse(diff3.edges().contains(e));
        assertEquals(0, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());
    }
}
