package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

public class IsolatedEquivalenceInvariantTest {

    private UniverseGraph graph;
    private Universe universe;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        EphemeralGraph eph = new EphemeralGraph(universe);

        a = eph.factory().createNode();
        b = eph.factory().createNode();
        c = eph.factory().createNode();
        d = eph.factory().createNode();
        e = eph.factory().createNode(); // isolated

        eph.addNode(a);
        eph.addNode(b);
        eph.addNode(c);
        eph.addNode(d);
        eph.addNode(e);

        eph.addEdge(eph.factory().createEdge(a, b));
        eph.addEdge(eph.factory().createEdge(b, c));
        eph.addEdge(eph.factory().createEdge(d, d)); // self-loop

        graph = universe.promote(eph);
    }

    @Test
    public void testIsolatedIsIntersectionOfRootsAndLeaves() {
        NodeSet isolated = graph.isolated();
        NodeSet rootsIntersectLeaves = graph.roots().intersect(graph.leaves());

        assertEquals(rootsIntersectLeaves.size(), isolated.size());
        for (Node n : rootsIntersectLeaves) {
            assertTrue(isolated.contains(n));
        }
        for (Node n : isolated) {
            assertTrue(rootsIntersectLeaves.contains(n));
        }
    }
}
