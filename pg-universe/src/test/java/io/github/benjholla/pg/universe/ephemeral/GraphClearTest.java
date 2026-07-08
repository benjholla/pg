package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class GraphClearTest {

    @Test
    public void testClear() {
        EphemeralFactory factory = new EphemeralGraph().factory();
        Graph graph = factory.createGraph();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);

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
        EphemeralFactory factory = new EphemeralGraph().factory();
        Graph graph = factory.createGraph();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);

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
