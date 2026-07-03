package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.GraphElement;
import io.github.benjholla.pg.api.Node;

public class GraphIdentityTest {

    private EphemeralGraph graph;
    private Node n1, n2;
    private Edge e1;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();
        n1 = new EphemeralNode();
        n2 = new EphemeralNode();
        e1 = new EphemeralEdge(n1, n2);

        graph.add(n1);
        graph.add(n2);
        graph.add(e1);
    }

    @Test
    public void testGetGraphElementById() {
        Optional<GraphElement> optN1 = graph.getGraphElementById(n1.id());
        assertTrue(optN1.isPresent(), "Node n1 should be found by id");
        assertEquals(n1.id(), optN1.get().id());

        Optional<GraphElement> optE1 = graph.getGraphElementById(e1.id());
        assertTrue(optE1.isPresent(), "Edge e1 should be found by id");
        assertEquals(e1.id(), optE1.get().id());

        Optional<GraphElement> notFound = graph.getGraphElementById(-999);
        assertFalse(notFound.isPresent(), "Non-existent element should return empty Optional");
    }

    @Test
    public void testGetNodeById() {
        Optional<Node> optN1 = graph.getNodeById(n1.id());
        assertTrue(optN1.isPresent(), "Node n1 should be found by id");
        assertEquals(n1.id(), optN1.get().id());

        Optional<Node> optE1 = graph.getNodeById(e1.id());
        assertFalse(optE1.isPresent(), "Edge id should not be found as a node");

        Optional<Node> notFound = graph.getNodeById(-999);
        assertFalse(notFound.isPresent(), "Non-existent node should return empty Optional");
    }

    @Test
    public void testGetEdgeById() {
        Optional<Edge> optE1 = graph.getEdgeById(e1.id());
        assertTrue(optE1.isPresent(), "Edge e1 should be found by id");
        assertEquals(e1.id(), optE1.get().id());

        Optional<Edge> optN1 = graph.getEdgeById(n1.id());
        assertFalse(optN1.isPresent(), "Node id should not be found as an edge");

        Optional<Edge> notFound = graph.getEdgeById(-999);
        assertFalse(notFound.isPresent(), "Non-existent edge should return empty Optional");
    }
}
