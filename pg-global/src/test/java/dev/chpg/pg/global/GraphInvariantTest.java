package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;

/**
 * Validates fundamental invariants of the Graph interface implementations.
 */
public class GraphInvariantTest {

    private GlobalGraph graph;
    private Node a, b, c;
    private Edge ab, bc, ca;

    @BeforeEach
    public void setUp() {
        a = new GlobalNode();
        b = new GlobalNode();
        c = new GlobalNode();

        ab = new GlobalEdge(a, b);
        bc = new GlobalEdge(b, c);
        ca = new GlobalEdge(c, a);

        graph = new GlobalGraph(a, b, c);
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);
    }

    @Test
    public void testRemoveNodeRemovesAssociatedEdges() {
        // Assert initial state
        assertTrue(graph.nodes().contains(a));
        assertTrue(graph.edges().contains(ab));
        assertTrue(graph.edges().contains(ca));

        // When node 'a' is removed
        boolean nodeRemoved = graph.removeNode(a);
        assertTrue(nodeRemoved);

        // Then node 'a' is gone
        assertFalse(graph.nodes().contains(a));

        // And incident edges must also be removed
        assertFalse(graph.edges().contains(ab), "Edge ab must be removed because node a was removed");
        assertFalse(graph.edges().contains(ca), "Edge ca must be removed because node a was removed");

        // Independent edges should remain
        assertTrue(graph.edges().contains(bc));
    }

    @Test
    public void testEdgeCannotExistWithoutTerminalNodes() {
        Node d = new GlobalNode();
        Node e = new GlobalNode();
        Edge de = new GlobalEdge(d, e);

        // Attempting to add an edge implicitly adds its nodes
        graph.addEdge(de);
        assertTrue(graph.nodes().contains(d));
        assertTrue(graph.nodes().contains(e));
        assertTrue(graph.edges().contains(de));

        // If we remove 'd', 'de' must disappear
        graph.removeNode(d);
        assertFalse(graph.edges().contains(de));
    }

    @Test
    public void testIsolateNode() {
        // Removing 'ab' and 'bc' leaves 'b' isolated but present
        graph.removeEdge(ab);
        graph.removeEdge(bc);

        assertTrue(graph.nodes().contains(b), "Node b should still exist after its edges are removed");
        assertFalse(graph.edges().contains(ab));
        assertFalse(graph.edges().contains(bc));
    }
}
