package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Node.NodeDirection;
import dev.chpg.pg.api.NodeSet;

public class GraphLimitInvariantTest {

    @Test
    public void testLimitInEquivalentToRoots() {
        Node a = new GlobalNode(); Node b = new GlobalNode(); Node c = new GlobalNode();
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));

        NodeSet limitIn = graph.limit(NodeDirection.IN);
        NodeSet roots = graph.roots();

        assertEquals(roots.size(), limitIn.size());
        assertTrue(roots.containsAll(limitIn));
        assertTrue(limitIn.containsAll(roots));
    }

    @Test
    public void testLimitOutEquivalentToLeaves() {
        Node a = new GlobalNode(); Node b = new GlobalNode(); Node c = new GlobalNode();
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));

        NodeSet limitOut = graph.limit(NodeDirection.OUT);
        NodeSet leaves = graph.leaves();

        assertEquals(leaves.size(), limitOut.size());
        assertTrue(leaves.containsAll(limitOut));
        assertTrue(limitOut.containsAll(leaves));
    }

    @Test
    public void testLimitEmptyGraph() {
        GlobalGraph graph = new GlobalGraph();

        assertTrue(graph.limit(NodeDirection.IN).isEmpty());
        assertTrue(graph.limit(NodeDirection.OUT).isEmpty());
    }

    @Test
    public void testLimitIsolatedNodes() {
        Node a = new GlobalNode();
        GlobalGraph graph = new GlobalGraph(a);

        NodeSet limitIn = graph.limit(NodeDirection.IN);
        assertEquals(1, limitIn.size());
        assertTrue(limitIn.contains(a));

        NodeSet limitOut = graph.limit(NodeDirection.OUT);
        assertEquals(1, limitOut.size());
        assertTrue(limitOut.contains(a));
    }

    @Test
    public void testLimitCyclicGraph() {
        Node a = new GlobalNode(); Node b = new GlobalNode(); Node c = new GlobalNode();
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(c, a));

        assertTrue(graph.limit(NodeDirection.IN).isEmpty());
        assertTrue(graph.limit(NodeDirection.OUT).isEmpty());
    }

    @Test
    public void testLimitNullHandling() {
        GlobalGraph graph = new GlobalGraph();
        assertThrows(NullPointerException.class, () -> graph.limit(null));
    }

    @Test
    public void testLimitBothIsolatedNodes() {
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        Node c = new GlobalNode();
        Node isolated = new GlobalNode();
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addNode(isolated);

        NodeSet limitBoth = graph.limit(NodeDirection.BOTH);

        assertEquals(1, limitBoth.size());
        assertTrue(limitBoth.contains(isolated));
    }
}
