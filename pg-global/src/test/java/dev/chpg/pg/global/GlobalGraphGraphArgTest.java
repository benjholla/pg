package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class GlobalGraphGraphArgTest {
    @Test
    public void testGraphArgMethods() {
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();
        Node n3 = new GlobalNode();
        Edge e1 = new GlobalEdge(n1, n2);
        Edge e2 = new GlobalEdge(n2, n3);
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(e1);
        graph.addEdge(e2);

        Graph g1 = new GlobalGraph(n2);

        assertEquals(1, graph.predecessors(g1).size());
        assertTrue(graph.predecessors(g1).contains(n1));

        assertEquals(1, graph.successors(g1).size());
        assertTrue(graph.successors(g1).contains(n3));

        Graph gFrom = new GlobalGraph(n1);
        Graph gTo = new GlobalGraph(n2);

        Graph betweenStep = graph.betweenStep(gFrom, gTo);
        assertEquals(2, betweenStep.nodes().size());
        assertTrue(betweenStep.edges().contains(e1));

        Graph gTo3 = new GlobalGraph(n3);
        Graph between = graph.between(gFrom, gTo3);
        assertEquals(3, between.nodes().size());
        assertTrue(between.edges().contains(e1));
        assertTrue(between.edges().contains(e2));
    }

    @Test
    public void testDifferenceEdgesBreak() {
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();
        Edge e1 = new GlobalEdge(n1, n2);
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(e1);

        Graph gRemove1 = new GlobalGraph(e1);
        Graph gRemove2 = new GlobalGraph(e1);

        Graph diff = graph.differenceEdges(gRemove1, gRemove2);
        assertTrue(diff.edges().isEmpty());
    }

    @Test
    public void testCreateGraphNullHandling() {
        GlobalGraph graph = new GlobalGraph();

        // Arrays
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph((Node[]) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph(new Node[]{null});
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph((Edge[]) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph(new Edge[]{null});
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph((Graph[]) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph(new Graph[]{null});
        });

        // Collections
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph((NodeSet) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph((EdgeSet) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph((NodeSet) null, new GlobalEdgeSet());
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph(new GlobalNodeSet(), (EdgeSet) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph((java.util.Collection<Graph>) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            graph.createGraph(java.util.Collections.singletonList(null));
        });
    }

    @Test
    public void testCreateGraphSuccess() {
        GlobalGraph graph = new GlobalGraph();
        Node n1 = graph.createNode();
        Node n2 = graph.createNode();
        Edge e1 = graph.createEdge(n1, n2);

        Graph gEmpty = graph.createGraph();
        assertTrue(gEmpty.isEmpty());

        Graph gNodes = graph.createGraph(n1, n2);
        assertEquals(2, gNodes.nodes().size());
        assertTrue(gNodes.edges().isEmpty());

        Graph gNodeSet = graph.createGraph(new GlobalNodeSet(n1, n2));
        assertEquals(2, gNodeSet.nodes().size());
        assertTrue(gNodeSet.edges().isEmpty());

        Graph gEdges = graph.createGraph(e1);
        assertEquals(2, gEdges.nodes().size());
        assertEquals(1, gEdges.edges().size());

        Graph gEdgeSet = graph.createGraph(new GlobalEdgeSet(e1));
        assertEquals(2, gEdgeSet.nodes().size());
        assertEquals(1, gEdgeSet.edges().size());

        Graph gNodesEdges = graph.createGraph(new GlobalNodeSet(n1, n2), new GlobalEdgeSet(e1));
        assertEquals(2, gNodesEdges.nodes().size());
        assertEquals(1, gNodesEdges.edges().size());

        Graph gGraphs = graph.createGraph(gNodes, gEdges);
        assertEquals(2, gGraphs.nodes().size());
        assertEquals(1, gGraphs.edges().size());

        Graph gGraphsColl = graph.createGraph(java.util.Arrays.asList(gNodes, gEdges));
        assertEquals(2, gGraphsColl.nodes().size());
        assertEquals(1, gGraphsColl.edges().size());
    }

    @Test
    public void testBetweenEmptyHandling() {
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();
        Node n3 = new GlobalNode();
        Edge e1 = new GlobalEdge(n1, n2);
        GlobalGraph graph = new GlobalGraph();
        graph.addEdge(e1);

        NodeSet emptyFrom = new GlobalNodeSet();
        NodeSet validTo = new GlobalNodeSet(n2);
        assertTrue(graph.between(emptyFrom, validTo).isEmpty());
        assertTrue(graph.betweenStep(emptyFrom, validTo).isEmpty());

        NodeSet validFrom = new GlobalNodeSet(n1);
        NodeSet emptyTo = new GlobalNodeSet();
        assertTrue(graph.between(validFrom, emptyTo).isEmpty());
        assertTrue(graph.betweenStep(validFrom, emptyTo).isEmpty());

        NodeSet isolated = new GlobalNodeSet(n3);
        assertTrue(graph.between(isolated, validTo).isEmpty());
        assertTrue(graph.betweenStep(isolated, validTo).isEmpty());
        assertTrue(graph.between(validFrom, isolated).isEmpty());
        assertTrue(graph.betweenStep(validFrom, isolated).isEmpty());
    }
}
