package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class IntersectionNodeInvariantTest {

    @Test
    public void testIntersectionNodeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph intersect0 = g0.intersection(n1);
        assertEquals(0, intersect0.nodes().size());
        assertEquals(0, intersect0.edges().size());

        // Case 2: graph has only the node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        Graph intersect1 = g1.intersection(n1);
        assertTrue(intersect1.nodes().contains(n1));
        assertEquals(1, intersect1.nodes().size());
        assertEquals(0, intersect1.edges().size());

        // Case 3: graph has the node, but intersecting with another node not in the graph
        Graph intersect1_n2 = g1.intersection(n2);
        assertEquals(0, intersect1_n2.nodes().size());
        assertEquals(0, intersect1_n2.edges().size());

        // Case 4: graph has both nodes and an edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e);

        Graph intersect3 = g3.intersection(n1);
        assertTrue(intersect3.nodes().contains(n1));
        assertFalse(intersect3.nodes().contains(n2));
        assertEquals(1, intersect3.nodes().size());
        assertEquals(0, intersect3.edges().size());
    }
}
