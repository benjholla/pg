package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class IsolatedIdentityInvariantTest {

    @Test
    public void testIsolatedIsRootsIntersectLeaves() {
        GlobalGraph graph = new GlobalGraph();
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
