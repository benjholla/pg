package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class IsolatedEquivalenceInvariantTest {

    private void assertIsolatedEquivalence(GlobalGraph graph) {
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
        GlobalGraph graph = new GlobalGraph();
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testSingleIsolatedNode() {
        GlobalGraph graph = new GlobalGraph();
        graph.addNode(new GlobalNode());
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testDisjointIsolatedNodes() {
        GlobalGraph graph = new GlobalGraph();
        graph.addNode(new GlobalNode());
        graph.addNode(new GlobalNode());
        graph.addNode(new GlobalNode());
        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testGraphWithEdgesAndIsolatedNodes() {
        GlobalGraph graph = new GlobalGraph();
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        Node c = new GlobalNode();
        Node d = new GlobalNode(); // isolated
        Node e = new GlobalNode(); // isolated

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);

        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));

        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testCyclicGraph() {
        GlobalGraph graph = new GlobalGraph();
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        Node c = new GlobalNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);

        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(c, a));

        assertIsolatedEquivalence(graph);
    }

    @Test
    public void testSelfLoop() {
        GlobalGraph graph = new GlobalGraph();
        Node a = new GlobalNode();
        graph.addNode(a);
        graph.addEdge(new GlobalEdge(a, a));

        assertIsolatedEquivalence(graph);
    }
}
