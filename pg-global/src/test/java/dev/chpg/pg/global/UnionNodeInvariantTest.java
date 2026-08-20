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
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph is empty
        Graph g0 = factory.createGraph();
        Graph union0 = g0.union(n1);
        assertEquals(1, union0.nodes().size());
        assertEquals(0, union0.edges().size());
        assertTrue(union0.nodes().contains(n1));

        // Case 2: graph already has the node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        Graph union1 = g1.union(n1);
        assertEquals(1, union1.nodes().size());
        assertEquals(0, union1.edges().size());
        assertTrue(union1.nodes().contains(n1));

        // Case 3: graph has a different node
        Graph g2 = factory.createGraph();
        g2.addNode(n2);
        Graph union2 = g2.union(n1);
        assertEquals(2, union2.nodes().size());
        assertEquals(0, union2.edges().size());
        assertTrue(union2.nodes().contains(n1));
        assertTrue(union2.nodes().contains(n2));

        // Case 4: graph has both nodes and an edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e);
        Graph union3 = g3.union(n1);
        assertEquals(2, union3.nodes().size());
        assertEquals(1, union3.edges().size());
        assertTrue(union3.nodes().contains(n1));
        assertTrue(union3.nodes().contains(n2));
        assertTrue(union3.edges().contains(e));
    }
}
