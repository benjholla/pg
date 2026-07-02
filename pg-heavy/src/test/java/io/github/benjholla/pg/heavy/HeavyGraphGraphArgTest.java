package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.NodeSet;

public class HeavyGraphGraphArgTest {
    @Test
    public void testGraphArgMethods() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Node n3 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        Edge e2 = new HeavyEdge(n2, n3);
        HeavyGraph graph = new HeavyGraph();
        graph.add(e1);
        graph.add(e2);

        Graph g1 = new HeavyGraph(n2);

        assertEquals(1, graph.predecessors(g1).size());
        assertTrue(graph.predecessors(g1).contains(n1));

        assertEquals(1, graph.successors(g1).size());
        assertTrue(graph.successors(g1).contains(n3));

        Graph gFrom = new HeavyGraph(n1);
        Graph gTo = new HeavyGraph(n2);

        Graph betweenStep = graph.betweenStep(gFrom, gTo);
        assertEquals(2, betweenStep.nodes().size());
        assertTrue(betweenStep.edges().contains(e1));

        Graph gTo3 = new HeavyGraph(n3);
        Graph between = graph.between(gFrom, gTo3);
        assertEquals(3, between.nodes().size());
        assertTrue(between.edges().contains(e1));
        assertTrue(between.edges().contains(e2));
    }

    @Test
    public void testDifferenceEdgesBreak() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        HeavyGraph graph = new HeavyGraph();
        graph.add(e1);

        Graph gRemove1 = new HeavyGraph(e1);
        Graph gRemove2 = new HeavyGraph(e1);

        Graph diff = graph.differenceEdges(gRemove1, gRemove2);
        assertTrue(diff.edges().isEmpty());
    }

    @Test
    public void testBetweenEmptyHandling() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Node n3 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        HeavyGraph graph = new HeavyGraph();
        graph.add(e1);

        NodeSet emptyFrom = new HeavyNodeSet();
        NodeSet validTo = new HeavyNodeSet(n2);
        assertTrue(graph.between(emptyFrom, validTo).isEmpty());
        assertTrue(graph.betweenStep(emptyFrom, validTo).isEmpty());

        NodeSet validFrom = new HeavyNodeSet(n1);
        NodeSet emptyTo = new HeavyNodeSet();
        assertTrue(graph.between(validFrom, emptyTo).isEmpty());
        assertTrue(graph.betweenStep(validFrom, emptyTo).isEmpty());

        NodeSet isolated = new HeavyNodeSet(n3);
        assertTrue(graph.between(isolated, validTo).isEmpty());
        assertTrue(graph.betweenStep(isolated, validTo).isEmpty());
        assertTrue(graph.between(validFrom, isolated).isEmpty());
        assertTrue(graph.betweenStep(validFrom, isolated).isEmpty());
    }
}
