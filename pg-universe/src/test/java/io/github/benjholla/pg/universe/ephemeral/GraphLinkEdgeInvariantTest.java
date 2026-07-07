package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

/**
 * Validates the invariant that linkEdge requires topological anchors.
 * Unlike addEdge, linkEdge must violently fail if the nodes aren't registered.
 */
public class GraphLinkEdgeInvariantTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    @Test
    public void testLinkEdgeSucceedsWhenNodesPresent() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = factory.createNode();
        Node b = factory.createNode();
        graph.addNode(a);
        graph.addNode(b);

        Edge edge = factory.createEdge(a, b);
        assertTrue(graph.linkEdge(edge), "linkEdge should return true when edge is successfully added");
        assertTrue(graph.edges().contains(edge), "Graph should contain the linked edge");
    }

    @Test
    public void testLinkEdgeFailsWhenFromNodeMissing() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = factory.createNode();
        Node b = factory.createNode();
        graph.addNode(b); // Only target is present

        Edge edge = factory.createEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when 'from' node is missing");
    }

    @Test
    public void testLinkEdgeFailsWhenToNodeMissing() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = factory.createNode();
        Node b = factory.createNode();
        graph.addNode(a); // Only source is present

        Edge edge = factory.createEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when 'to' node is missing");
    }

    @Test
    public void testLinkEdgeFailsWhenBothNodesMissing() {
        EphemeralGraph graph = new EphemeralGraph();
        Node a = factory.createNode();
        Node b = factory.createNode();

        Edge edge = factory.createEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when both nodes are missing");
    }
}
