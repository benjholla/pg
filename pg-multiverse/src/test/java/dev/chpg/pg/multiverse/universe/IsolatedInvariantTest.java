package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

/**
 * Validates properties related to isolated nodes in graphs.
 */
public class IsolatedInvariantTest {

    private UniverseGraph graph;
    private Node a, b, c, d, e, f, g;
    private Universe universe;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        EphemeralGraph eph = new EphemeralGraph(universe);
        a = eph.factory().createNode();
        b = eph.factory().createNode();
        c = eph.factory().createNode();
        d = eph.factory().createNode();
        e = eph.factory().createNode();
        f = eph.factory().createNode();
        g = eph.factory().createNode();

        eph.addNode(a);
        eph.addNode(b);
        eph.addNode(c);
        eph.addNode(d);
        eph.addNode(e);
        eph.addNode(f);
        eph.addNode(g);

        eph.addEdge(eph.factory().createEdge(a, b));
        eph.addEdge(eph.factory().createEdge(b, c));
        eph.addEdge(eph.factory().createEdge(d, d));

        eph.addEdge(eph.factory().createEdge(e, f));
        eph.addEdge(eph.factory().createEdge(f, e));

        graph = universe.promote(eph);

        // Wait, promote returns a graph with positive IDs but the old nodes have negative ids.
        // We re-query them.
        a = graph.nodes().ids().stream().filter(id -> id == -a.id()).map(id -> graph.node(id).get()).findFirst().orElse(null);
        b = graph.nodes().ids().stream().filter(id -> id == -b.id()).map(id -> graph.node(id).get()).findFirst().orElse(null);
        c = graph.nodes().ids().stream().filter(id -> id == -c.id()).map(id -> graph.node(id).get()).findFirst().orElse(null);
        d = graph.nodes().ids().stream().filter(id -> id == -d.id()).map(id -> graph.node(id).get()).findFirst().orElse(null);
        e = graph.nodes().ids().stream().filter(id -> id == -e.id()).map(id -> graph.node(id).get()).findFirst().orElse(null);
        f = graph.nodes().ids().stream().filter(id -> id == -f.id()).map(id -> graph.node(id).get()).findFirst().orElse(null);
        g = graph.nodes().ids().stream().filter(id -> id == -g.id()).map(id -> graph.node(id).get()).findFirst().orElse(null);
    }

    @Test
    public void testIsolatedNodesHaveNoInAndNoOutEdges() {
        NodeSet isolated = graph.isolated();

        assertEquals(1, isolated.size(), "Only one isolated node");

        // Assert all nodes in isolated have no in/out edges
        for (Node n : isolated) {
            assertTrue(graph.predecessors(n).isEmpty());
            assertTrue(graph.successors(n).isEmpty());
        }

        // Assert other nodes are not isolated
        int nonIsolatedCount = 0;
        for (Node n : graph.nodes()) {
            if (!isolated.contains(n)) {
                assertFalse(graph.predecessors(n).isEmpty() && graph.successors(n).isEmpty());
                nonIsolatedCount++;
            }
        }
        assertEquals(6, nonIsolatedCount);
    }

    @Test
    public void testIsolatedNodesEmptyGraph() {
        Universe u2 = new Universe();
        UniverseGraph emptyGraph = u2.promote(new EphemeralGraph(u2));
        assertTrue(emptyGraph.isolated().isEmpty(), "Empty graph should have no isolated nodes");
    }

    @Test
    public void testIsolatedNodesDisjointNodes() {
        Universe u3 = new Universe();
        EphemeralGraph eph = new EphemeralGraph(u3);
        eph.addNode(eph.factory().createNode());
        eph.addNode(eph.factory().createNode());

        UniverseGraph disjointGraph = u3.promote(eph);

        NodeSet isolated = disjointGraph.isolated();
        assertEquals(2, isolated.size(), "All nodes should be isolated");
        for (Node n : disjointGraph.nodes()) {
             assertTrue(isolated.contains(n));
        }
    }
}
