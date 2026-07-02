package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraphNullHandlingTest {
    private HeavyGraph graph;

    @BeforeEach
    public void setup() {
        graph = new HeavyGraph();
    }

    @Test
    public void testUnionNullArray() {
        assertThrows(NullPointerException.class, () -> graph.union((Graph[]) null));
    }

    @Test
    public void testUnionArrayWithNulls() {
        assertThrows(NullPointerException.class, () -> graph.union(new Graph[]{null}));
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
        assertThrows(NullPointerException.class, () -> graph.difference((Graph[]) null));
        assertThrows(NullPointerException.class, () -> graph.difference(new Graph[]{null}));
    }

    @Test
    public void testDifferenceEdgesNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.differenceEdges((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.differenceEdges(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> graph.differenceEdges((Graph[]) null));
        assertThrows(NullPointerException.class, () -> graph.differenceEdges(new Graph[]{null}));
    }

    @Test
    public void testIntersectionNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.intersection((Node[]) null));
        assertThrows(NullPointerException.class, () -> graph.intersection(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> graph.intersection((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.intersection(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> graph.intersection((Graph[]) null));
        assertThrows(NullPointerException.class, () -> graph.intersection(new Graph[]{null}));
    }

    @Test
    public void testInduceNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.induce((Edge[]) null));
        assertThrows(NullPointerException.class, () -> graph.induce(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> graph.induce((Graph[]) null));
        assertThrows(NullPointerException.class, () -> graph.induce(new Graph[]{null}));
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
    public void testBetweenNullHandling() {
        Node n = new HeavyNode();
        Graph g = new HeavyGraph();
        NodeSet ns = new HeavyNodeSet();
        assertThrows(NullPointerException.class, () -> graph.between((Node) null, n));
        assertThrows(NullPointerException.class, () -> graph.between(n, (Node) null));
        assertThrows(NullPointerException.class, () -> graph.between((Graph) null, g));
        assertThrows(NullPointerException.class, () -> graph.between(g, (Graph) null));
        assertThrows(NullPointerException.class, () -> graph.between((NodeSet) null, ns));
        assertThrows(NullPointerException.class, () -> graph.between(ns, (NodeSet) null));
    }

    @Test
    public void testBetweenStepNullHandling() {
        Node n = new HeavyNode();
        Graph g = new HeavyGraph();
        NodeSet ns = new HeavyNodeSet();
        assertThrows(NullPointerException.class, () -> graph.betweenStep((Node) null, n));
        assertThrows(NullPointerException.class, () -> graph.betweenStep(n, (Node) null));
        assertThrows(NullPointerException.class, () -> graph.betweenStep((Graph) null, g));
        assertThrows(NullPointerException.class, () -> graph.betweenStep(g, (Graph) null));
        assertThrows(NullPointerException.class, () -> graph.betweenStep((NodeSet) null, ns));
        assertThrows(NullPointerException.class, () -> graph.betweenStep(ns, (NodeSet) null));
    }
}
