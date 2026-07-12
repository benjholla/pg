package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class RootsLeavesInvariantTest {

    @Test
    public void testRootsHaveNoInEdges() {
        Node a = new GlobalNode(); Node b = new GlobalNode(); Node c = new GlobalNode();
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));

        NodeSet roots = graph.roots();
        for (Node root : roots) {
            assertTrue(graph.predecessors(root).isEmpty(), "Root node must not have predecessors");
            // check via reverse step
            Graph rStep = graph.reverseStep(root);
            assertEquals(1, rStep.nodes().size(), "Reverse step from root should only contain the root itself");
            assertEquals(0, rStep.edges().size(), "Reverse step from root should contain 0 edges");
        }

        assertEquals(1, roots.size());
        assertTrue(roots.contains(a));
    }

    @Test
    public void testLeavesHaveNoOutEdges() {
        Node a = new GlobalNode(); Node b = new GlobalNode(); Node c = new GlobalNode();
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));

        NodeSet leaves = graph.leaves();
        for (Node leaf : leaves) {
            assertTrue(graph.successors(leaf).isEmpty(), "Leaf node must not have successors");
            // check via forward step
            Graph fStep = graph.forwardStep(leaf);
            assertEquals(1, fStep.nodes().size(), "Forward step from leaf should only contain the leaf itself");
            assertEquals(0, fStep.edges().size(), "Forward step from leaf should contain 0 edges");
        }

        assertEquals(1, leaves.size());
        assertTrue(leaves.contains(c));
    }

    @Test
    public void testIsolatedNodesAreBothRootsAndLeaves() {
        Node isolated = new GlobalNode();
        GlobalGraph graph = new GlobalGraph(isolated);

        assertTrue(graph.roots().contains(isolated), "Isolated node is a root");
        assertTrue(graph.leaves().contains(isolated), "Isolated node is a leaf");
    }

    @Test
    public void testCyclicGraphHasNoRootsOrLeaves() {
        Node a = new GlobalNode(); Node b = new GlobalNode(); Node c = new GlobalNode();
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(c, a));

        assertTrue(graph.roots().isEmpty(), "Cyclic graph with no ingress nodes has no roots");
        assertTrue(graph.leaves().isEmpty(), "Cyclic graph with no egress nodes has no leaves");
    }
}
