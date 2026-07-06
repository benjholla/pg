package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class RootsLeavesInvariantTest {

    @Test
    public void testRootsHaveNoInEdges() {
        Node a = (HeavyNode) new HeavyGraph().createNode(); Node b = (HeavyNode) new HeavyGraph().createNode(); Node c = (HeavyNode) new HeavyGraph().createNode();
        HeavyGraph graph = new HeavyGraph();
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(a, b));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(b, c));

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
        Node a = (HeavyNode) new HeavyGraph().createNode(); Node b = (HeavyNode) new HeavyGraph().createNode(); Node c = (HeavyNode) new HeavyGraph().createNode();
        HeavyGraph graph = new HeavyGraph();
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(a, b));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(b, c));

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
        Node isolated = (HeavyNode) new HeavyGraph().createNode();
        HeavyGraph graph = (HeavyGraph) new HeavyGraph().createGraph(isolated);

        assertTrue(graph.roots().contains(isolated), "Isolated node is a root");
        assertTrue(graph.leaves().contains(isolated), "Isolated node is a leaf");
    }

    @Test
    public void testCyclicGraphHasNoRootsOrLeaves() {
        Node a = (HeavyNode) new HeavyGraph().createNode(); Node b = (HeavyNode) new HeavyGraph().createNode(); Node c = (HeavyNode) new HeavyGraph().createNode();
        HeavyGraph graph = new HeavyGraph();
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(a, b));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(b, c));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(c, a));

        assertTrue(graph.roots().isEmpty(), "Cyclic graph with no ingress nodes has no roots");
        assertTrue(graph.leaves().isEmpty(), "Cyclic graph with no egress nodes has no leaves");
    }
}
