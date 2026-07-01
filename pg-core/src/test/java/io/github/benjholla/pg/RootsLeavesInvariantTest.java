package io.github.benjholla.pg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RootsLeavesInvariantTest {

    @Test
    public void testRootsHaveNoInEdges() {
        Node a = new Node(); Node b = new Node(); Node c = new Node();
        HeavyGraph graph = new HeavyGraph();
        graph.add(new Edge(a, b));
        graph.add(new Edge(b, c));

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
        Node a = new Node(); Node b = new Node(); Node c = new Node();
        HeavyGraph graph = new HeavyGraph();
        graph.add(new Edge(a, b));
        graph.add(new Edge(b, c));

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
        Node isolated = new Node();
        HeavyGraph graph = new HeavyGraph(isolated);

        assertTrue(graph.roots().contains(isolated), "Isolated node is a root");
        assertTrue(graph.leaves().contains(isolated), "Isolated node is a leaf");
    }

    @Test
    public void testCyclicGraphHasNoRootsOrLeaves() {
        Node a = new Node(); Node b = new Node(); Node c = new Node();
        HeavyGraph graph = new HeavyGraph();
        graph.add(new Edge(a, b));
        graph.add(new Edge(b, c));
        graph.add(new Edge(c, a));

        assertTrue(graph.roots().isEmpty(), "Cyclic graph with no ingress nodes has no roots");
        assertTrue(graph.leaves().isEmpty(), "Cyclic graph with no egress nodes has no leaves");
    }
}
