package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.universe.Universe;

/**
 * Validates properties related to isolated nodes in graphs.
 */
public class IsolatedInvariantTest {

    private EphemeralGraph graph;
    private Node a, b, c, d, e, f, g;
    private Universe universe;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        graph = new EphemeralGraph(universe);
        a = graph.factory().createNode();
        b = graph.factory().createNode();
        c = graph.factory().createNode();
        d = graph.factory().createNode();
        e = graph.factory().createNode();
        f = graph.factory().createNode();
        g = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);
        graph.addNode(f);
        graph.addNode(g);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(b, c));
        graph.addEdge(graph.factory().createEdge(d, d));

        graph.addEdge(graph.factory().createEdge(e, f));
        graph.addEdge(graph.factory().createEdge(f, e));
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
        EphemeralGraph emptyGraph = new EphemeralGraph(universe);
        assertTrue(emptyGraph.isolated().isEmpty(), "Empty graph should have no isolated nodes");
    }

    @Test
    public void testIsolatedNodesDisjointNodes() {
        EphemeralGraph disjointGraph = new EphemeralGraph(universe);
        Node n1 = disjointGraph.factory().createNode();
        Node n2 = disjointGraph.factory().createNode();

        disjointGraph.addNode(n1);
        disjointGraph.addNode(n2);

        NodeSet isolated = disjointGraph.isolated();
        assertEquals(2, isolated.size(), "All nodes should be isolated");
        assertTrue(isolated.contains(n1));
        assertTrue(isolated.contains(n2));
    }
}
