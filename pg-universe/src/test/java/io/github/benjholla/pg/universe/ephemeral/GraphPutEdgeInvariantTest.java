package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

/**
 * Validates the invariant that putEdge requires topological anchors.
 * Unlike addEdge, putEdge must violently fail if the nodes aren't registered.
 */
public class GraphPutEdgeInvariantTest {

    @Test
    public void testPutEdgeSucceedsWhenNodesPresent() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = new EphemeralNode();
        Node b = new EphemeralNode();
        graph.addNode(a);
        graph.addNode(b);

        Edge edge = new EphemeralEdge(a, b);
        assertTrue(graph.putEdge(edge), "putEdge should return true when edge is successfully added");
        assertTrue(graph.edges().contains(edge), "Graph should contain the put edge");
    }

    @Test
    public void testPutEdgeFailsWhenFromNodeMissing() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = new EphemeralNode();
        Node b = new EphemeralNode();
        graph.addNode(b); // Only target is present

        Edge edge = new EphemeralEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.putEdge(edge),
                "putEdge should throw IllegalArgumentException when 'from' node is missing");
    }

    @Test
    public void testPutEdgeFailsWhenToNodeMissing() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = new EphemeralNode();
        Node b = new EphemeralNode();
        graph.addNode(a); // Only source is present

        Edge edge = new EphemeralEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.putEdge(edge),
                "putEdge should throw IllegalArgumentException when 'to' node is missing");
    }

    @Test
    public void testPutEdgeFailsWhenBothNodesMissing() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = new EphemeralNode();
        Node b = new EphemeralNode();

        Edge edge = new EphemeralEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.putEdge(edge),
                "putEdge should throw IllegalArgumentException when both nodes are missing");
    }
}
