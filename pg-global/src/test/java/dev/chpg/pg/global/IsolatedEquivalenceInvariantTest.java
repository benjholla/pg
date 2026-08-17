package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class IsolatedEquivalenceInvariantTest {

    private GlobalGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        a = new GlobalNode();
        b = new GlobalNode();
        c = new GlobalNode();
        d = new GlobalNode();
        e = new GlobalNode(); // isolated

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);

        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(d, d)); // self-loop
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
