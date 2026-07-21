package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class GraphNullHandlingTest {
    private GlobalGraph graph;

    @BeforeEach
    public void setup() {
        graph = new GlobalGraph();
    }

    @Test
    public void testUnionNodes() {
        assertThrows(NullPointerException.class, () -> graph.union((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.union(new Node[]{null}));
    }

    @Test
    public void testUnionEdges() {
        assertThrows(NullPointerException.class, () -> graph.union((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.union(new Edge[]{null}));
    }

    @Test
    public void testDifferenceNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.difference((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.difference(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.difference((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.difference(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> graph.difference((Graph) null));
    }

    @Test
    public void testDifferenceEdgesNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.differenceEdges((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.differenceEdges(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> graph.differenceEdges((Graph) null));
    }

    @Test
    public void testIntersectionNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.intersection((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.intersection(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.intersection((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.intersection(new Edge[]{null}));
    }

    @Test
    public void testInduceNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.induce((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.induce(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> graph.induce((EdgeSet) null));
    }

    @Test
    public void testForwardNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.forward((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.forward(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.forward((NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.forward((Graph) null));
    }

    @Test
    public void testReverseNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.reverse((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.reverse(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.reverse((NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.reverse((Graph) null));
    }

    @Test
    public void testForwardStepNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.forwardStep((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.forwardStep(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.forwardStep((NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.forwardStep((Graph) null));
    }

    @Test
    public void testReverseStepNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.reverseStep((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.reverseStep(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.reverseStep((NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.reverseStep((Graph) null));
    }

    @Test
    public void testPredecessorsNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.predecessors((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.predecessors(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.predecessors((NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.predecessors((Graph) null));
    }

    @Test
    public void testSuccessorsNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.successors((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.successors(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.successors((NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.successors((Graph) null));
    }

    @Test
    public void testBetweenNullHandling() {
        Node n = new GlobalNode();
        Graph g = new GlobalGraph();
        NodeSet ns = new GlobalNodeSet();
        assertThrows(NullPointerException.class, () -> graph.between((Node) null, n));
        assertThrows(NullPointerException.class, () -> graph.between(n, (Node) null));
        assertThrows(NullPointerException.class, () -> graph.between((Graph) null, g));
        assertThrows(NullPointerException.class, () -> graph.between(g, (Graph) null));
        assertThrows(NullPointerException.class, () -> graph.between((NodeSet) null, ns));
        assertThrows(NullPointerException.class, () -> graph.between(ns, (NodeSet) null));
    }

    @Test
    public void testBetweenStepNullHandling() {
        Node n = new GlobalNode();
        Graph g = new GlobalGraph();
        NodeSet ns = new GlobalNodeSet();
        assertThrows(NullPointerException.class, () -> graph.betweenStep((Node) null, n));
        assertThrows(NullPointerException.class, () -> graph.betweenStep(n, (Node) null));
        assertThrows(NullPointerException.class, () -> graph.betweenStep((Graph) null, g));
        assertThrows(NullPointerException.class, () -> graph.betweenStep(g, (Graph) null));
        assertThrows(NullPointerException.class, () -> graph.betweenStep((NodeSet) null, ns));
        assertThrows(NullPointerException.class, () -> graph.betweenStep(ns, (NodeSet) null));
    }

    @Test
    public void testRetainAllNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.retainAllNodes(null));
        assertThrows(NullPointerException.class, () -> graph.retainAllNodes(Collections.singletonList(null)));
        assertThrows(NullPointerException.class, () -> graph.retainAllEdges(null));
        assertThrows(NullPointerException.class, () -> graph.retainAllEdges(Collections.singletonList(null)));
    }

    @Test
    public void testRemoveAllNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.removeAllNodes(null));
        assertThrows(NullPointerException.class, () -> graph.removeAllNodes(Collections.singletonList(null)));
        assertThrows(NullPointerException.class, () -> graph.removeAllEdges(null));
        assertThrows(NullPointerException.class, () -> graph.removeAllEdges(Collections.singletonList(null)));
    }

    @Test
    public void testAddAllNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.addAllNodes(null));
        assertThrows(NullPointerException.class, () -> graph.addAllNodes(Collections.singletonList(null)));
        assertThrows(NullPointerException.class, () -> graph.addAllEdges(null));
        assertThrows(NullPointerException.class, () -> graph.addAllEdges(Collections.singletonList(null)));
    }

    @Test
    public void testLinkAllEdgesNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.linkAllEdges(null));
        assertThrows(NullPointerException.class, () -> graph.linkAllEdges(Collections.singletonList(null)));
    }
}
