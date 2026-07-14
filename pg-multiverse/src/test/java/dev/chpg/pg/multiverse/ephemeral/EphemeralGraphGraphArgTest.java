package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class EphemeralGraphGraphArgTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    @Test
    public void testGraphArgMethods() {
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);
        Edge e2 = factory.createEdge(n2, n3);
        Graph graph = factory.createGraph();
        graph.addEdge(e1);
        graph.addEdge(e2);

        Graph g1 = factory.createGraph(n2);

        assertEquals(1, graph.predecessors(g1).size());
        assertTrue(graph.predecessors(g1).contains(n1));

        assertEquals(1, graph.successors(g1).size());
        assertTrue(graph.successors(g1).contains(n3));

        Graph gFrom = factory.createGraph(n1);
        Graph gTo = factory.createGraph(n2);

        Graph betweenStep = graph.betweenStep(gFrom, gTo);
        assertEquals(2, betweenStep.nodes().size());
        assertTrue(betweenStep.edges().contains(e1));

        Graph gTo3 = factory.createGraph(n3);
        Graph between = graph.between(gFrom, gTo3);
        assertEquals(3, between.nodes().size());
        assertTrue(between.edges().contains(e1));
        assertTrue(between.edges().contains(e2));
    }

    @Test
    public void testDifferenceEdgesBreak() {
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);
        Graph graph = factory.createGraph();
        graph.addEdge(e1);

        Graph gRemove1 = factory.createGraph(e1);
        Graph gRemove2 = factory.createGraph(e1);

        Graph diff = graph.differenceEdges(gRemove1, gRemove2);
        assertTrue(diff.edges().isEmpty());
    }

    @Test
    public void testBetweenEmptyHandling() {
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);
        Graph graph = factory.createGraph();
        graph.addEdge(e1);

        NodeSet emptyFrom = new EphemeralNodeSet();
        NodeSet validTo = new EphemeralNodeSet(n2);
        assertTrue(graph.between(emptyFrom, validTo).isEmpty());
        assertTrue(graph.betweenStep(emptyFrom, validTo).isEmpty());

        NodeSet validFrom = new EphemeralNodeSet(n1);
        NodeSet emptyTo = new EphemeralNodeSet();
        assertTrue(graph.between(validFrom, emptyTo).isEmpty());
        assertTrue(graph.betweenStep(validFrom, emptyTo).isEmpty());

        NodeSet isolated = new EphemeralNodeSet(n3);
        assertTrue(graph.between(isolated, validTo).isEmpty());
        assertTrue(graph.betweenStep(isolated, validTo).isEmpty());
        assertTrue(graph.between(validFrom, isolated).isEmpty());
        assertTrue(graph.betweenStep(validFrom, isolated).isEmpty());
    }
}
