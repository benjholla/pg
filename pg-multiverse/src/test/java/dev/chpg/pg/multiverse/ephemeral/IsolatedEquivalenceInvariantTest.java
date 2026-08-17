package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.universe.Universe;

public class IsolatedEquivalenceInvariantTest {

    private EphemeralGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        Universe universe = new Universe();
        graph = new EphemeralGraph(universe);

        a = graph.factory().createNode();
        b = graph.factory().createNode();
        c = graph.factory().createNode();
        d = graph.factory().createNode();
        e = graph.factory().createNode(); // isolated

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(b, c));
        graph.addEdge(graph.factory().createEdge(d, d)); // self-loop
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
