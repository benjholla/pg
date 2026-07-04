package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.NodeSet;

public class EphemeralGraphGraphArgTest {
    @Test
    public void testGraphArgMethods() {
        Node n1 = new EphemeralNode();
        Node n2 = new EphemeralNode();
        Node n3 = new EphemeralNode();
        Edge e1 = new EphemeralEdge(n1, n2);
        Edge e2 = new EphemeralEdge(n2, n3);
        EphemeralGraph graph = new EphemeralGraph();
        graph.addEdge(e1);
        graph.addEdge(e2);

        Graph g1 = new EphemeralGraph(n2);

        assertEquals(1, graph.predecessors(g1).size());
        assertTrue(graph.predecessors(g1).contains(n1));

        assertEquals(1, graph.successors(g1).size());
        assertTrue(graph.successors(g1).contains(n3));

        Graph gFrom = new EphemeralGraph(n1);
        Graph gTo = new EphemeralGraph(n2);

        Graph betweenStep = graph.betweenStep(gFrom, gTo);
        assertEquals(2, betweenStep.nodes().size());
        assertTrue(betweenStep.edges().contains(e1));

        Graph gTo3 = new EphemeralGraph(n3);
        Graph between = graph.between(gFrom, gTo3);
        assertEquals(3, between.nodes().size());
        assertTrue(between.edges().contains(e1));
        assertTrue(between.edges().contains(e2));
    }

    @Test
    public void testDifferenceEdgesBreak() {
        Node n1 = new EphemeralNode();
        Node n2 = new EphemeralNode();
        Edge e1 = new EphemeralEdge(n1, n2);
        EphemeralGraph graph = new EphemeralGraph();
        graph.addEdge(e1);

        Graph gRemove1 = new EphemeralGraph(e1);
        Graph gRemove2 = new EphemeralGraph(e1);

        Graph diff = graph.differenceEdges(gRemove1, gRemove2);
        assertTrue(diff.edges().isEmpty());
    }

    @Test
    public void testBetweenEmptyHandling() {
        Node n1 = new EphemeralNode();
        Node n2 = new EphemeralNode();
        Node n3 = new EphemeralNode();
        Edge e1 = new EphemeralEdge(n1, n2);
        EphemeralGraph graph = new EphemeralGraph();
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
