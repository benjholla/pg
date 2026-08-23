package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

public class IsolatedEquivalenceInvariantTest {

    private Universe universe;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
    }

    private void assertIsolatedEquivalence(UniverseGraph graph) {
        NodeSet isolated = graph.isolated();
        NodeSet rootsIntersectLeaves = graph.roots().intersect(graph.leaves());

        assertEquals(isolated.size(), rootsIntersectLeaves.size(), "Isolated set size should match intersection of roots and leaves");

        for (Node n : isolated) {
            assertTrue(rootsIntersectLeaves.contains(n), "Roots intersect leaves should contain isolated node");
        }

        for (Node n : rootsIntersectLeaves) {
            assertTrue(isolated.contains(n), "Isolated should contain roots intersect leaves node");
        }
    }

    @Test
    public void testEmptyGraph() {
        EphemeralGraph eph = new EphemeralGraph(universe);
        UniverseGraph graph = universe.promote(eph);
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testSingleIsolatedNode() {
        EphemeralGraph eph = new EphemeralGraph(universe);
        eph.addNode(eph.factory().createNode());
        UniverseGraph graph = universe.promote(eph);
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testDisjointIsolatedNodes() {
        EphemeralGraph eph = new EphemeralGraph(universe);
        eph.addNode(eph.factory().createNode());
        eph.addNode(eph.factory().createNode());
        eph.addNode(eph.factory().createNode());
        UniverseGraph graph = universe.promote(eph);
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testGraphWithEdgesAndIsolatedNodes() {
        EphemeralGraph eph = new EphemeralGraph(universe);
        Node a = eph.factory().createNode();
        Node b = eph.factory().createNode();
        Node c = eph.factory().createNode();
        Node d = eph.factory().createNode(); // isolated
        Node e = eph.factory().createNode(); // isolated

        eph.addNode(a);
        eph.addNode(b);
        eph.addNode(c);
        eph.addNode(d);
        eph.addNode(e);

        eph.addEdge(eph.factory().createEdge(a, b));
        eph.addEdge(eph.factory().createEdge(b, c));

        UniverseGraph graph = universe.promote(eph);
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testCyclicGraph() {
        EphemeralGraph eph = new EphemeralGraph(universe);
        Node a = eph.factory().createNode();
        Node b = eph.factory().createNode();
        Node c = eph.factory().createNode();

        eph.addNode(a);
        eph.addNode(b);
        eph.addNode(c);

        eph.addEdge(eph.factory().createEdge(a, b));
        eph.addEdge(eph.factory().createEdge(b, c));
        eph.addEdge(eph.factory().createEdge(c, a));

        UniverseGraph graph = universe.promote(eph);
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testSelfLoop() {
        EphemeralGraph eph = new EphemeralGraph(universe);
        Node a = eph.factory().createNode();
        eph.addNode(a);
        eph.addEdge(eph.factory().createEdge(a, a));

        UniverseGraph graph = universe.promote(eph);
        assertIsolatedEquivalence(graph);
    }
}
