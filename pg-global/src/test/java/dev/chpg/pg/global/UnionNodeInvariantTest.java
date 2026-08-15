package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnionNodeInvariantTest {

    @Test
    public void testUnionNodeInvariant() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e12 = factory.createEdge(n1, n2);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph union0 = g0.union(n1);
        assertEquals(1, union0.nodes().size());
        assertEquals(0, union0.edges().size());
        assertTrue(union0.nodes().contains(n1));

        // Case 2: graph has other node
        Graph g1 = factory.createGraph();
        g1.addNode(n2);
        Graph union1 = g1.union(n1);
        assertEquals(2, union1.nodes().size());
        assertEquals(0, union1.edges().size());
        assertTrue(union1.nodes().contains(n1));
        assertTrue(union1.nodes().contains(n2));

        // Case 3: graph has the node already
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        Graph union2 = g2.union(n1);
        assertEquals(1, union2.nodes().size());
        assertEquals(0, union2.edges().size());
        assertTrue(union2.nodes().contains(n1));

        // Case 4: graph has both nodes and an edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e12);

        Graph union3 = g3.union(n3);
        assertEquals(3, union3.nodes().size());
        assertEquals(1, union3.edges().size());
        assertTrue(union3.nodes().contains(n1));
        assertTrue(union3.nodes().contains(n2));
        assertTrue(union3.nodes().contains(n3));
        assertTrue(union3.edges().contains(e12));

        // Case 5: idempotent addition does not disturb edges
        Graph union4 = g3.union(n1);
        assertEquals(2, union4.nodes().size());
        assertEquals(1, union4.edges().size());
        assertTrue(union4.nodes().contains(n1));
        assertTrue(union4.nodes().contains(n2));
        assertTrue(union4.edges().contains(e12));
    }
}
