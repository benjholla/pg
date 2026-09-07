package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.universe.Universe;

public class IsolatedIdentityInvariantTest {

    @Test
    public void testIsolatedIsRootsIntersectLeaves() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);

        graph.addEdge(graph.factory().createEdge(a, b));

        NodeSet isolated = graph.isolated();
        NodeSet rootsIntersectLeaves = graph.roots().intersect(graph.leaves());

        assertEquals(rootsIntersectLeaves.size(), isolated.size());
        assertTrue(isolated.containsAll(rootsIntersectLeaves));
        assertTrue(rootsIntersectLeaves.containsAll(isolated));
    }
}
