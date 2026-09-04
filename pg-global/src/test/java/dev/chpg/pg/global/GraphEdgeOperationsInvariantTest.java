package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphEdgeOperationsInvariantTest {

    @Test
    public void testGraphEdgeOperationsInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph has neither node
        Graph g0 = factory.createGraph();

        Graph intersect0 = g0.intersection(e);
        assertEquals(0, intersect0.nodes().size());
        assertEquals(0, intersect0.edges().size());

        Graph diff0 = g0.difference(e);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        Graph diffEdge0 = g0.differenceEdges(e);
        assertEquals(0, diffEdge0.nodes().size());
        assertEquals(0, diffEdge0.edges().size());

        Graph induce0 = g0.induce(e);
        assertEquals(0, induce0.nodes().size());
        assertEquals(0, induce0.edges().size());

        // Case 2: graph has one node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        g1.addNode(n3);

        Graph intersect1 = g1.intersection(e);
        assertTrue(intersect1.nodes().contains(n1));
        assertFalse(intersect1.nodes().contains(n2));
        assertFalse(intersect1.nodes().contains(n3));
        assertEquals(1, intersect1.nodes().size());
        assertEquals(0, intersect1.edges().size());

        Graph diff1 = g1.difference(e);
        assertFalse(diff1.nodes().contains(n1)); // n1 should be removed
        assertTrue(diff1.nodes().contains(n3));
        assertEquals(1, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        Graph diffEdge1 = g1.differenceEdges(e);
        assertTrue(diffEdge1.nodes().contains(n1));
        assertTrue(diffEdge1.nodes().contains(n3));
        assertEquals(2, diffEdge1.nodes().size());
        assertEquals(0, diffEdge1.edges().size());

        Graph induce1 = g1.induce(e);
        assertEquals(2, induce1.nodes().size());
        assertEquals(0, induce1.edges().size());

        // Case 3: graph has both nodes, but not the edge
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        g2.addNode(n3);

        Graph intersect2 = g2.intersection(e);
        assertTrue(intersect2.nodes().contains(n1));
        assertTrue(intersect2.nodes().contains(n2));
        assertFalse(intersect2.nodes().contains(n3));
        assertEquals(2, intersect2.nodes().size());
        assertEquals(0, intersect2.edges().size());

        Graph diff2 = g2.difference(e);
        assertFalse(diff2.nodes().contains(n1));
        assertFalse(diff2.nodes().contains(n2));
        assertTrue(diff2.nodes().contains(n3));
        assertEquals(1, diff2.nodes().size());
        assertEquals(0, diff2.edges().size());

        Graph diffEdge2 = g2.differenceEdges(e);
        assertTrue(diffEdge2.nodes().contains(n1));
        assertTrue(diffEdge2.nodes().contains(n2));
        assertTrue(diffEdge2.nodes().contains(n3));
        assertEquals(3, diffEdge2.nodes().size());
        assertEquals(0, diffEdge2.edges().size());

        Graph induce2 = g2.induce(e);
        assertEquals(3, induce2.nodes().size());
        assertEquals(1, induce2.edges().size());
        assertTrue(induce2.edges().contains(e));

        // Case 4: graph has both nodes and the edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addNode(n3);
        g3.addEdge(e);

        Graph intersect3 = g3.intersection(e);
        assertTrue(intersect3.nodes().contains(n1));
        assertTrue(intersect3.nodes().contains(n2));
        assertFalse(intersect3.nodes().contains(n3));
        assertTrue(intersect3.edges().contains(e));
        assertEquals(2, intersect3.nodes().size());
        assertEquals(1, intersect3.edges().size());

        Graph diff3 = g3.difference(e);
        assertFalse(diff3.nodes().contains(n1));
        assertFalse(diff3.nodes().contains(n2));
        assertTrue(diff3.nodes().contains(n3));
        assertFalse(diff3.edges().contains(e));
        assertEquals(1, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());

        Graph diffEdge3 = g3.differenceEdges(e);
        assertTrue(diffEdge3.nodes().contains(n1));
        assertTrue(diffEdge3.nodes().contains(n2));
        assertTrue(diffEdge3.nodes().contains(n3));
        assertFalse(diffEdge3.edges().contains(e));
        assertEquals(3, diffEdge3.nodes().size());
        assertEquals(0, diffEdge3.edges().size());

        Graph induce3 = g3.induce(e);
        assertEquals(3, induce3.nodes().size());
        assertEquals(1, induce3.edges().size());
        assertTrue(induce3.edges().contains(e));
    }
}
