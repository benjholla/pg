package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
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
