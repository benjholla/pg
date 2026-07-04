package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Node.NodeDirection;
import io.github.benjholla.pg.api.NodeSet;

public class GraphLimitInvariantTest {

    @Test
    public void testLimitInEquivalentToRoots() {
        Node a = new HeavyNode(); Node b = new HeavyNode(); Node c = new HeavyNode();
        HeavyGraph graph = new HeavyGraph();
        graph.addEdge(new HeavyEdge(a, b));
        graph.addEdge(new HeavyEdge(b, c));

        NodeSet limitIn = graph.limit(NodeDirection.IN);
        NodeSet roots = graph.roots();

        assertEquals(roots.size(), limitIn.size());
        assertTrue(roots.containsAll(limitIn));
        assertTrue(limitIn.containsAll(roots));
    }

    @Test
    public void testLimitOutEquivalentToLeaves() {
        Node a = new HeavyNode(); Node b = new HeavyNode(); Node c = new HeavyNode();
        HeavyGraph graph = new HeavyGraph();
        graph.addEdge(new HeavyEdge(a, b));
        graph.addEdge(new HeavyEdge(b, c));

        NodeSet limitOut = graph.limit(NodeDirection.OUT);
        NodeSet leaves = graph.leaves();

        assertEquals(leaves.size(), limitOut.size());
        assertTrue(leaves.containsAll(limitOut));
        assertTrue(limitOut.containsAll(leaves));
    }

    @Test
    public void testLimitEmptyGraph() {
        HeavyGraph graph = new HeavyGraph();

        assertTrue(graph.limit(NodeDirection.IN).isEmpty());
        assertTrue(graph.limit(NodeDirection.OUT).isEmpty());
    }

    @Test
    public void testLimitIsolatedNodes() {
        Node a = new HeavyNode();
        HeavyGraph graph = new HeavyGraph(a);

        NodeSet limitIn = graph.limit(NodeDirection.IN);
        assertEquals(1, limitIn.size());
        assertTrue(limitIn.contains(a));

        NodeSet limitOut = graph.limit(NodeDirection.OUT);
        assertEquals(1, limitOut.size());
        assertTrue(limitOut.contains(a));
    }

    @Test
    public void testLimitCyclicGraph() {
        Node a = new HeavyNode(); Node b = new HeavyNode(); Node c = new HeavyNode();
        HeavyGraph graph = new HeavyGraph();
        graph.addEdge(new HeavyEdge(a, b));
        graph.addEdge(new HeavyEdge(b, c));
        graph.addEdge(new HeavyEdge(c, a));

        assertTrue(graph.limit(NodeDirection.IN).isEmpty());
        assertTrue(graph.limit(NodeDirection.OUT).isEmpty());
    }

    @Test
    public void testLimitNullHandling() {
        HeavyGraph graph = new HeavyGraph();
        assertThrows(NullPointerException.class, () -> graph.limit(null));
    }
}
