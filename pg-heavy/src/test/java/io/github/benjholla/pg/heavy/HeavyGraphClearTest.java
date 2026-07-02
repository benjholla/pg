package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class HeavyGraphClearTest {
    @Test
    public void testClearEdges() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        HeavyGraph graph = new HeavyGraph();
        graph.add(e1);
        graph.edges().clear();
        assertTrue(graph.edges().isEmpty());
        assertEquals(2, graph.nodes().size());
        assertTrue(graph.predecessors(n2).isEmpty());
        assertTrue(graph.successors(n1).isEmpty());
    }

    @Test
    public void testIteratorRemove() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        HeavyGraph graph = new HeavyGraph();
        graph.add(e1);
        Iterator<Edge> it = graph.edges().iterator();
        assertTrue(it.hasNext());
        Edge current = it.next();
        assertEquals(e1, current);
        it.remove();
        assertTrue(graph.edges().isEmpty());
        assertEquals(2, graph.nodes().size());
        assertTrue(graph.predecessors(n2).isEmpty());
        assertTrue(graph.successors(n1).isEmpty());
    }
}
