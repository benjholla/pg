package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.universe.Universe;

public class IsolatedEquivalenceInvariantTest {

    private Universe universe;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
    }

    private void assertIsolatedEquivalence(EphemeralGraph graph) {
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
        EphemeralGraph graph = new EphemeralGraph(universe);
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testSingleIsolatedNode() {
        EphemeralGraph graph = new EphemeralGraph(universe);
        graph.addNode(graph.factory().createNode());
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testDisjointIsolatedNodes() {
        EphemeralGraph graph = new EphemeralGraph(universe);
        graph.addNode(graph.factory().createNode());
        graph.addNode(graph.factory().createNode());
        graph.addNode(graph.factory().createNode());
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testGraphWithEdgesAndIsolatedNodes() {
        EphemeralGraph graph = new EphemeralGraph(universe);
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode(); // isolated
        Node e = graph.factory().createNode(); // isolated

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(b, c));

        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testCyclicGraph() {
        EphemeralGraph graph = new EphemeralGraph(universe);
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(b, c));
        graph.addEdge(graph.factory().createEdge(c, a));

        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testSelfLoop() {
        EphemeralGraph graph = new EphemeralGraph(universe);
        Node a = graph.factory().createNode();
        graph.addNode(a);
        graph.addEdge(graph.factory().createEdge(a, a));

        assertIsolatedEquivalence(graph);
    }
}
