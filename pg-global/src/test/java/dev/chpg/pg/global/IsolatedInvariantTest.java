package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

/**
 * Validates properties related to isolated nodes in graphs.
 */
public class IsolatedInvariantTest {

    private GlobalGraph graph;
    private Node a, b, c, d, e, f, g;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        a = new GlobalNode();
        b = new GlobalNode();
        c = new GlobalNode();
        d = new GlobalNode();
        e = new GlobalNode();
        f = new GlobalNode();
        g = new GlobalNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);
        graph.addNode(f);
        graph.addNode(g);

        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(d, d));

        graph.addEdge(new GlobalEdge(e, f));
        graph.addEdge(new GlobalEdge(f, e));
    }

    @Test
    public void testIsolatedNodesHaveNoInAndNoOutEdges() {
        NodeSet isolated = graph.isolated();

        assertEquals(1, isolated.size(), "Only one isolated node");
        assertTrue(isolated.contains(g), "Node g should be isolated");

        assertFalse(isolated.contains(a), "Node a has out edge");
        assertFalse(isolated.contains(b), "Node b has in and out edge");
        assertFalse(isolated.contains(c), "Node c has in edge");
        assertFalse(isolated.contains(d), "Node d has self loop");
        assertFalse(isolated.contains(e), "Node e has in and out edge");
        assertFalse(isolated.contains(f), "Node f has in and out edge");
    }

    @Test
    public void testIsolatedNodesEmptyGraph() {
        GlobalGraph emptyGraph = new GlobalGraph();
        assertTrue(emptyGraph.isolated().isEmpty(), "Empty graph should have no isolated nodes");
    }

    @Test
    public void testIsolatedNodesDisjointNodes() {
        GlobalGraph disjointGraph = new GlobalGraph();
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();

        disjointGraph.addNode(n1);
        disjointGraph.addNode(n2);

        NodeSet isolated = disjointGraph.isolated();
        assertEquals(2, isolated.size(), "All nodes should be isolated");
        assertTrue(isolated.contains(n1));
        assertTrue(isolated.contains(n2));
    }
}
