package io.github.benjholla.pg.universe.ephemeral;

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
        Node a = new EphemeralNode(); Node b = new EphemeralNode(); Node c = new EphemeralNode();
        EphemeralGraph graph = new EphemeralGraph();
        graph.addEdge(new EphemeralEdge(a, b));
        graph.addEdge(new EphemeralEdge(b, c));

        NodeSet limitIn = graph.limit(NodeDirection.IN);
        NodeSet roots = graph.roots();

        assertEquals(roots.size(), limitIn.size());
        assertTrue(roots.containsAll(limitIn));
        assertTrue(limitIn.containsAll(roots));
    }

    @Test
    public void testLimitOutEquivalentToLeaves() {
        Node a = new EphemeralNode(); Node b = new EphemeralNode(); Node c = new EphemeralNode();
        EphemeralGraph graph = new EphemeralGraph();
        graph.addEdge(new EphemeralEdge(a, b));
        graph.addEdge(new EphemeralEdge(b, c));

        NodeSet limitOut = graph.limit(NodeDirection.OUT);
        NodeSet leaves = graph.leaves();

        assertEquals(leaves.size(), limitOut.size());
        assertTrue(leaves.containsAll(limitOut));
        assertTrue(limitOut.containsAll(leaves));
    }

    @Test
    public void testLimitEmptyGraph() {
        EphemeralGraph graph = new EphemeralGraph();

        assertTrue(graph.limit(NodeDirection.IN).isEmpty());
        assertTrue(graph.limit(NodeDirection.OUT).isEmpty());
    }

    @Test
    public void testLimitIsolatedNodes() {
        Node a = new EphemeralNode();
        EphemeralGraph graph = new EphemeralGraph(a);

        NodeSet limitIn = graph.limit(NodeDirection.IN);
        assertEquals(1, limitIn.size());
        assertTrue(limitIn.contains(a));

        NodeSet limitOut = graph.limit(NodeDirection.OUT);
        assertEquals(1, limitOut.size());
        assertTrue(limitOut.contains(a));
    }

    @Test
    public void testLimitCyclicGraph() {
        Node a = new EphemeralNode(); Node b = new EphemeralNode(); Node c = new EphemeralNode();
        EphemeralGraph graph = new EphemeralGraph();
        graph.addEdge(new EphemeralEdge(a, b));
        graph.addEdge(new EphemeralEdge(b, c));
        graph.addEdge(new EphemeralEdge(c, a));

        assertTrue(graph.limit(NodeDirection.IN).isEmpty());
        assertTrue(graph.limit(NodeDirection.OUT).isEmpty());
    }

    @Test
    public void testLimitNullHandling() {
        EphemeralGraph graph = new EphemeralGraph();
        assertThrows(NullPointerException.class, () -> graph.limit(null));
    }
}
