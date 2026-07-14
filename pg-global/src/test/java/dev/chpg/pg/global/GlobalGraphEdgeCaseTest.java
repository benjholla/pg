package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class GlobalGraphEdgeCaseTest {

    @Test
    public void testUnionWithNull() {
        GlobalGraph graph = new GlobalGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }

    @Test
    public void testSingletonNode() {
        GlobalGraph graph = new GlobalGraph();
        GlobalNode n = new GlobalNode();
        dev.chpg.pg.api.NodeSet ns = graph.singleton(n);
        org.junit.jupiter.api.Assertions.assertEquals(1, ns.size());
        org.junit.jupiter.api.Assertions.assertTrue(ns.contains(n));
    }

    @Test
    public void testSingletonEdge() {
        GlobalGraph graph = new GlobalGraph();
        GlobalEdge e = new GlobalEdge(new GlobalNode(), new GlobalNode());
        dev.chpg.pg.api.EdgeSet es = graph.singleton(e);
        org.junit.jupiter.api.Assertions.assertEquals(1, es.size());
        org.junit.jupiter.api.Assertions.assertTrue(es.contains(e));
    }
}
