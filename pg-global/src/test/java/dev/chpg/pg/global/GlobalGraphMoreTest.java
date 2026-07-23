package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Node.NodeDirection;
import dev.chpg.pg.api.NodeSet;

public class GlobalGraphMoreTest {

    @Test
    public void testExceptionOnForeignElements() {
        GlobalGraph g = new GlobalGraph();
        Node foreignNode = new dev.chpg.pg.api.Node() {
            @Override public int id() { return 999; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        Edge foreignEdge = new dev.chpg.pg.api.Edge() {
            @Override public int id() { return 999; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            @Override public Node from() { return foreignNode; }
            @Override public Node to() { return foreignNode; }
        };

        assertThrows(IllegalArgumentException.class, () -> g.addNode(foreignNode));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(foreignEdge));
        assertFalse(g.removeNode(foreignNode));
    }

    @Test
    public void testEdgesBothDirection() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();

        Edge e1 = factory.createEdge(n1, n2);
        Edge e2 = factory.createEdge(n2, n3);
        g.addEdge(e1);
        g.addEdge(e2);

        EdgeSet both = g.edges(n2, NodeDirection.BOTH);
        assertEquals(2, both.size());
        assertTrue(both.contains(e1));
        assertTrue(both.contains(e2));
    }

    @Test
    public void testIntersectionSingleNode() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n1 = factory.createNode();
        g.addNode(n1);

        Graph intersection = g.intersection(n1);
        assertEquals(1, intersection.nodes().size());
        assertTrue(intersection.nodes().contains(n1));
    }

    @Test
    public void testBetweenStepEmpty() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();

        g.addNode(n1);
        g.addNode(n2);
        g.addNode(n3);

        // n1 has no forward edges
        Graph b1 = g.betweenStep(n1, n2);
        assertTrue(b1.nodes().isEmpty());

        Edge e = factory.createEdge(n1, n2);
        g.addEdge(e);

        // n3 has no reverse edges
        Graph b2 = g.betweenStep(n1, n3);
        assertTrue(b2.nodes().isEmpty());
    }

    @Test
    public void testBetweenEmpty() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();

        g.addNode(n1);
        g.addNode(n2);
        g.addNode(n3);

        // n1 has no forward path
        Graph b1 = g.between(n1, n2);
        assertTrue(b1.nodes().isEmpty());

        Edge e = factory.createEdge(n1, n2);
        g.addEdge(e);

        // n3 has no reverse path
        Graph b2 = g.between(n1, n3);
        assertTrue(b2.nodes().isEmpty());
    }

    @Test
    public void testInduceEdge() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);
        g.addEdge(e);

        Graph induced = g.induce(e);
        assertEquals(2, induced.nodes().size());
        assertEquals(1, induced.edges().size());
        assertTrue(induced.edges().contains(e));
    }

    @Test
    public void testSingleton() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n = factory.createNode();
        Edge e = factory.createEdge(n, n);

        NodeSet ns = g.singleton(n);
        assertEquals(1, ns.size());
        assertTrue(ns.contains(n));

        EdgeSet es = g.singleton(e);
        assertEquals(1, es.size());
        assertTrue(es.contains(e));
    }

    @Test
    public void testBetweenStepAndBetweenDisconnected() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Node n4 = factory.createNode();

        g.addNode(n1);
        g.addNode(n2);
        g.addNode(n3);
        g.addNode(n4);

        Edge e1 = factory.createEdge(n1, n2);
        g.addEdge(e1);
        Edge e2 = factory.createEdge(n3, n4);
        g.addEdge(e2);

        // n1 has forward path, n4 has reverse path, but they don't intersect
        Graph bStep = g.betweenStep(n1, n4);
        assertTrue(bStep.nodes().isEmpty());

        Graph bFull = g.between(n1, n4);
        assertTrue(bFull.nodes().isEmpty());
    }

    @Test
    public void testBetweenStepAndBetweenDisconnected2() {
        GlobalGraph g = new GlobalGraph();
        GlobalFactory factory = g.factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        g.addNode(n1);
        g.addNode(n2);

        Graph bStep = g.betweenStep(n1, n2);
        assertTrue(bStep.nodes().isEmpty());

        Graph bFull = g.between(n1, n2);
        assertTrue(bFull.nodes().isEmpty());
    }
}
