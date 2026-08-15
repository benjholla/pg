package dev.chpg.pg.multiverse;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnionEdgeInvariantTest {

    @Test
    public void testUnionEdgeInvariantEphemeral() {
        Universe u = new Universe();
        EphemeralFactory factory = new EphemeralGraph(u).factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);

        // Case 1: graph has neither node
        Graph g0 = factory.createGraph();
        Graph union0 = g0.union(e1);
        assertEquals(2, union0.nodes().size());
        assertEquals(1, union0.edges().size());
        assertTrue(union0.nodes().contains(n1));
        assertTrue(union0.nodes().contains(n2));
        assertTrue(union0.edges().contains(e1));

        // Case 2: graph has one node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        Graph union1 = g1.union(e1);
        assertEquals(2, union1.nodes().size());
        assertEquals(1, union1.edges().size());
        assertTrue(union1.nodes().contains(n1));
        assertTrue(union1.nodes().contains(n2));
        assertTrue(union1.edges().contains(e1));

        // Case 3: graph has both nodes, but not the edge
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        Graph union2 = g2.union(e1);
        assertEquals(2, union2.nodes().size());
        assertEquals(1, union2.edges().size());
        assertTrue(union2.nodes().contains(n1));
        assertTrue(union2.nodes().contains(n2));
        assertTrue(union2.edges().contains(e1));

        // Case 4: graph has both nodes and the edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e1);
        Graph union3 = g3.union(e1);
        assertEquals(2, union3.nodes().size());
        assertEquals(1, union3.edges().size());
        assertTrue(union3.nodes().contains(n1));
        assertTrue(union3.nodes().contains(n2));
        assertTrue(union3.edges().contains(e1));
    }
}
