package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;

public class GraphClearTest {

    @Test
    public void testClear() {
        GlobalGraph graph = new GlobalGraph();
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();
        Edge e1 = new GlobalEdge(n1, n2);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);

        assertEquals(2, graph.nodes().size());
        assertEquals(1, graph.edges().size());

        graph.clear();

        assertEquals(0, graph.nodes().size());
        assertEquals(0, graph.edges().size());
        assertTrue(graph.isEmpty());
    }

    @Test
    public void testClearEdges() {
        GlobalGraph graph = new GlobalGraph();
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();
        Edge e1 = new GlobalEdge(n1, n2);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);

        assertEquals(2, graph.nodes().size());
        assertEquals(1, graph.edges().size());

        graph.clearEdges();

        assertEquals(2, graph.nodes().size());
        assertEquals(0, graph.edges().size());
    }

}
