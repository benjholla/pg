package io.github.benjholla.pg.heavy;

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

    @Test
    public void testLinkEdgeSucceedsWhenNodesPresent() {
        HeavyGraph graph = new HeavyGraph();
        Node a = new HeavyNode();
        Node b = new HeavyNode();
        graph.addNode(a);
        graph.addNode(b);

        Edge edge = new HeavyEdge(a, b);
        assertTrue(graph.linkEdge(edge), "linkEdge should return true when edge is successfully added");
        assertTrue(graph.edges().contains(edge), "Graph should contain the linked edge");
    }

    @Test
    public void testLinkEdgeFailsWhenFromNodeMissing() {
        HeavyGraph graph = new HeavyGraph();
        Node a = new HeavyNode();
        Node b = new HeavyNode();
        graph.addNode(b); // Only target is present

        Edge edge = new HeavyEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when 'from' node is missing");
    }

    @Test
    public void testLinkEdgeFailsWhenToNodeMissing() {
        HeavyGraph graph = new HeavyGraph();
        Node a = new HeavyNode();
        Node b = new HeavyNode();
        graph.addNode(a); // Only source is present

        Edge edge = new HeavyEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when 'to' node is missing");
    }

    @Test
    public void testLinkEdgeFailsWhenBothNodesMissing() {
        HeavyGraph graph = new HeavyGraph();
        Node a = new HeavyNode();
        Node b = new HeavyNode();

        Edge edge = new HeavyEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when both nodes are missing");
    }
}
