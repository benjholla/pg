package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InduceEdgeInvariantTest {

    @Test
    public void testInduceEdgeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);

        // Case 1: empty graph
        Graph g0 = factory.createGraph();
        Graph induce0 = g0.induce(e1);
        assertEquals(0, induce0.nodes().size());
        assertEquals(0, induce0.edges().size());

        // Case 2: graph has 1 node, not the other
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        Graph induce1 = g1.induce(e1);
        assertEquals(1, induce1.nodes().size());
        assertEquals(0, induce1.edges().size());
        assertTrue(induce1.nodes().contains(n1));

        // Case 3: graph has both nodes
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        Graph induce2 = g2.induce(e1);
        assertEquals(2, induce2.nodes().size());
        assertEquals(1, induce2.edges().size());
        assertTrue(induce2.nodes().contains(n1));
        assertTrue(induce2.nodes().contains(n2));
        assertTrue(induce2.edges().contains(e1));

        // Case 4: graph has both nodes and the edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e1);
        Graph induce3 = g3.induce(e1);
        assertEquals(2, induce3.nodes().size());
        assertEquals(1, induce3.edges().size());
        assertTrue(induce3.nodes().contains(n1));
        assertTrue(induce3.nodes().contains(n2));
        assertTrue(induce3.edges().contains(e1));
    }
}
