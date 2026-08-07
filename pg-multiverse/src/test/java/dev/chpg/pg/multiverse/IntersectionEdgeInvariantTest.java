package dev.chpg.pg.multiverse;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntersectionEdgeInvariantTest {

    @Test
    public void testIntersectionEdgeInvariantEphemeral() {
        Universe u = new Universe();
        EphemeralFactory factory = new EphemeralGraph(u).factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph has neither node
        Graph g0 = factory.createGraph();
        Graph intersect0 = g0.intersection(e);
        assertEquals(0, intersect0.nodes().size());
        assertEquals(0, intersect0.edges().size());

        // Case 2: graph has one node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        Graph intersect1 = g1.intersection(e);
        assertTrue(intersect1.nodes().contains(n1));
        assertFalse(intersect1.nodes().contains(n2));
        assertEquals(1, intersect1.nodes().size());
        assertEquals(0, intersect1.edges().size());

        // Case 3: graph has both nodes, but not the edge
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        Graph intersect2 = g2.intersection(e);
        assertTrue(intersect2.nodes().contains(n1));
        assertTrue(intersect2.nodes().contains(n2));
        assertEquals(2, intersect2.nodes().size());
        assertEquals(0, intersect2.edges().size());

        // Case 4: graph has both nodes and the edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e);
        Graph intersect3 = g3.intersection(e);
        assertTrue(intersect3.nodes().contains(n1));
        assertTrue(intersect3.nodes().contains(n2));
        assertTrue(intersect3.edges().contains(e));
        assertEquals(2, intersect3.nodes().size());
        assertEquals(1, intersect3.edges().size());
    }
}
