package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
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

        Graph diff = graph.differenceEdges(gRemove1).differenceEdges(gRemove2);
        assertTrue(diff.edges().isEmpty());
    }

    @Test
    public void testCreateGraphNullHandling() {
        // Arrays
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            factory.createGraph((Node) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            factory.createGraph((Edge) null);
        });

        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            factory.createGraph((Graph) null);
        });

        // Collections
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            factory.createGraph((NodeSet) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            factory.createGraph((EdgeSet) null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            factory.createGraph((NodeSet) null, new EphemeralEdgeSet());
        });
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            factory.createGraph(new EphemeralNodeSet(), (EdgeSet) null);
        });
    }

    @Test
    public void testCreateGraphSuccess() {
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);

        Graph gEmpty = factory.createGraph();
        assertTrue(gEmpty.nodes().isEmpty());

        Graph gNodes = factory.createGraph(new EphemeralNodeSet(n1, n2));
        assertEquals(2, gNodes.nodes().size());
        assertTrue(gNodes.edges().isEmpty());

        Graph gNodeSet = factory.createGraph(new EphemeralNodeSet(n1, n2));
        assertEquals(2, gNodeSet.nodes().size());
        assertTrue(gNodeSet.edges().isEmpty());

        Graph gEdges = factory.createGraph(e1);
        assertEquals(2, gEdges.nodes().size());
        assertEquals(1, gEdges.edges().size());

        Graph gEdgeSet = factory.createGraph(new EphemeralEdgeSet(e1));
        assertEquals(2, gEdgeSet.nodes().size());
        assertEquals(1, gEdgeSet.edges().size());

        Graph gNodesEdges = factory.createGraph(new EphemeralNodeSet(n1, n2), new EphemeralEdgeSet(e1));
        assertEquals(2, gNodesEdges.nodes().size());
        assertEquals(1, gNodesEdges.edges().size());
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
        assertTrue(graph.between(emptyFrom, validTo).nodes().isEmpty());
        assertTrue(graph.betweenStep(emptyFrom, validTo).nodes().isEmpty());

        NodeSet validFrom = new EphemeralNodeSet(n1);
        NodeSet emptyTo = new EphemeralNodeSet();
        assertTrue(graph.between(validFrom, emptyTo).nodes().isEmpty());
        assertTrue(graph.betweenStep(validFrom, emptyTo).nodes().isEmpty());

        NodeSet isolated = new EphemeralNodeSet(n3);
        assertTrue(graph.between(isolated, validTo).nodes().isEmpty());
        assertTrue(graph.betweenStep(isolated, validTo).nodes().isEmpty());
        assertTrue(graph.between(validFrom, isolated).nodes().isEmpty());
        assertTrue(graph.betweenStep(validFrom, isolated).nodes().isEmpty());
    }
}
