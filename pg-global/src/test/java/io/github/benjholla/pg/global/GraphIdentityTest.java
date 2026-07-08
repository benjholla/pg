package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class GraphIdentityTest {

    private GlobalGraph graph;
    private Node n1, n2;
    private Edge e1;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        n1 = new GlobalNode();
        n2 = new GlobalNode();
        e1 = new GlobalEdge(n1, n2);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);
    }

    @Test
    public void testGetNodeById() {
        Optional<Node> optN1 = graph.node(n1.id());
        assertTrue(optN1.isPresent(), "Node n1 should be found by id");
        assertEquals(n1.id(), optN1.get().id());

        // Node and Edge IDs can overlap in GlobalGraph, so we can't test that e1.id() doesn't exist as a node

        Optional<Node> notFound = graph.node(-999);
        assertFalse(notFound.isPresent(), "Non-existent node should return empty Optional");
    }

    @Test
    public void testGetEdgeById() {
        Optional<Edge> optE1 = graph.edge(e1.id());
        assertTrue(optE1.isPresent(), "Edge e1 should be found by id");
        assertEquals(e1.id(), optE1.get().id());

        // Node and Edge IDs can overlap in GlobalGraph, so we can't test that n1.id() doesn't exist as an edge

        Optional<Edge> notFound = graph.edge(-999);
        assertFalse(notFound.isPresent(), "Non-existent edge should return empty Optional");
    }
}
