package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates fundamental invariants of the Graph interface implementations.
 */
public class GraphInvariantTest {

    private HeavyGraph graph;
    private Node a, b, c;
    private Edge ab, bc, ca;

    @BeforeEach
    public void setUp() {
        a = new HeavyNode();
        b = new HeavyNode();
        c = new HeavyNode();

        ab = new HeavyEdge(a, b);
        bc = new HeavyEdge(b, c);
        ca = new HeavyEdge(c, a);

        graph = new HeavyGraph(a, b, c);
        graph.add(ab);
        graph.add(bc);
        graph.add(ca);
    }

    @Test
    public void testRemoveNodeRemovesAssociatedEdges() {
        // Assert initial state
        assertTrue(graph.nodes().contains(a));
        assertTrue(graph.edges().contains(ab));
        assertTrue(graph.edges().contains(ca));

        // When node 'a' is removed
        boolean nodeRemoved = graph.remove(a);
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
        Node d = new HeavyNode();
        Node e = new HeavyNode();
        Edge de = new HeavyEdge(d, e);

        // Attempting to add an edge implicitly adds its nodes
        graph.add(de);
        assertTrue(graph.nodes().contains(d));
        assertTrue(graph.nodes().contains(e));
        assertTrue(graph.edges().contains(de));

        // If we remove 'd', 'de' must disappear
        graph.remove(d);
        assertFalse(graph.edges().contains(de));
    }

    @Test
    public void testIsolateNode() {
        // Removing 'ab' and 'bc' leaves 'b' isolated but present
        graph.remove(ab);
        graph.remove(bc);

        assertTrue(graph.nodes().contains(b), "Node b should still exist after its edges are removed");
        assertFalse(graph.edges().contains(ab));
        assertFalse(graph.edges().contains(bc));
    }
}
