package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

/**
 * Validates fundamental invariants of the Graph interface implementations.
 */
public class GraphInvariantTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    private EphemeralGraph graph;
    private Node a, b, c;
    private Edge ab, bc, ca;

    @BeforeEach
    public void setUp() {
        a = factory.createNode();
        b = factory.createNode();
        c = factory.createNode();

        ab = factory.createEdge(a, b);
        bc = factory.createEdge(b, c);
        ca = factory.createEdge(c, a);

        graph = factory.createGraph(a, b, c);
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
        Node d = factory.createNode();
        Node e = factory.createNode();
        Edge de = factory.createEdge(d, e);

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
