package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;

/**
 * Validates the invariant that linkEdge requires topological anchors.
 * Unlike addEdge, linkEdge must violently fail if the nodes aren't registered.
 */
public class GraphLinkEdgeInvariantTest {

    @Test
    public void testLinkEdgeSucceedsWhenNodesPresent() {
        GlobalGraph graph = new GlobalGraph();
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        graph.addNode(a);
        graph.addNode(b);

        Edge edge = new GlobalEdge(a, b);
        assertTrue(graph.linkEdge(edge), "linkEdge should return true when edge is successfully added");
        assertTrue(graph.edges().contains(edge), "Graph should contain the linked edge");
    }

    @Test
    public void testLinkEdgeFailsWhenFromNodeMissing() {
        GlobalGraph graph = new GlobalGraph();
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        graph.addNode(b); // Only target is present

        Edge edge = new GlobalEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when 'from' node is missing");
    }

    @Test
    public void testLinkEdgeFailsWhenToNodeMissing() {
        GlobalGraph graph = new GlobalGraph();
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        graph.addNode(a); // Only source is present

        Edge edge = new GlobalEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when 'to' node is missing");
    }

    @Test
    public void testLinkEdgeFailsWhenBothNodesMissing() {
        GlobalGraph graph = new GlobalGraph();
        Node a = new GlobalNode();
        Node b = new GlobalNode();

        Edge edge = new GlobalEdge(a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.linkEdge(edge),
                "linkEdge should throw IllegalArgumentException when both nodes are missing");
    }
}
