package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class GraphRetainAllTest {

    @Test
    public void testRetainAllNodes() {
        GlobalGraph graph = new GlobalGraph();
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();
        Node n3 = new GlobalNode();
        Edge e1 = new GlobalEdge(n1, n2);
        Edge e2 = new GlobalEdge(n2, n3);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addEdge(e1);
        graph.addEdge(e2);

        assertEquals(3, graph.nodes().size());
        assertEquals(2, graph.edges().size());

        boolean changed = graph.retainAllNodes(Arrays.asList(n1, n2));
        assertTrue(changed);

        assertEquals(2, graph.nodes().size());
        assertTrue(graph.containsNode(n1));
        assertTrue(graph.containsNode(n2));
        assertFalse(graph.containsNode(n3));

        assertEquals(1, graph.edges().size());
        assertTrue(graph.containsEdge(e1));
        assertFalse(graph.containsEdge(e2)); // e2 should be removed because n3 is removed

        boolean changedAgain = graph.retainAllNodes(Arrays.asList(n1, n2));
        assertFalse(changedAgain);
    }

    @Test
    public void testRetainAllEdges() {
        GlobalGraph graph = new GlobalGraph();
        Node n1 = new GlobalNode();
        Node n2 = new GlobalNode();
        Node n3 = new GlobalNode();
        Edge e1 = new GlobalEdge(n1, n2);
        Edge e2 = new GlobalEdge(n2, n3);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addEdge(e1);
        graph.addEdge(e2);

        assertEquals(3, graph.nodes().size());
        assertEquals(2, graph.edges().size());

        boolean changed = graph.retainAllEdges(Arrays.asList(e1));
        assertTrue(changed);

        assertEquals(3, graph.nodes().size()); // nodes shouldn't be removed
        assertEquals(1, graph.edges().size());
        assertTrue(graph.containsEdge(e1));
        assertFalse(graph.containsEdge(e2));

        boolean changedAgain = graph.retainAllEdges(Arrays.asList(e1));
        assertFalse(changedAgain);
    }
}
