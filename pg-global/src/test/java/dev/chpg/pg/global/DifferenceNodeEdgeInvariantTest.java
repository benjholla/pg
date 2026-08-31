package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DifferenceNodeEdgeInvariantTest {

    @Test
    public void testDifferenceNodeCascadingEdgeRemoval() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();

        Edge e12 = factory.createEdge(n1, n2);
        Edge e23 = factory.createEdge(n2, n3);

        Graph g = factory.createGraph();
        g.addNode(n1);
        g.addNode(n2);
        g.addNode(n3);
        g.addEdge(e12);
        g.addEdge(e23);

        // Subtracting n2 should remove n2 and cascade remove e12 and e23
        Graph diff = g.difference(n2);

        assertTrue(diff.nodes().contains(n1));
        assertFalse(diff.nodes().contains(n2));
        assertTrue(diff.nodes().contains(n3));

        assertEquals(2, diff.nodes().size());
        assertEquals(0, diff.edges().size());
    }
}
