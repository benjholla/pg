package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class GraphIdentityTest {

    private HeavyGraph graph;
    private Node n1, n2;
    private Edge e1;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        n1 = (HeavyNode) new HeavyGraph().createNode();
        n2 = (HeavyNode) new HeavyGraph().createNode();
        e1 = (HeavyEdge) new HeavyGraph().createEdge(n1, n2);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);
    }

    @Test
    public void testGetNodeById() {
        Optional<Node> optN1 = graph.node(n1.id());
        assertTrue(optN1.isPresent(), "Node n1 should be found by id");
        assertEquals(n1.id(), optN1.get().id());

        Optional<Node> optE1 = graph.node(e1.id());
        assertFalse(optE1.isPresent(), "Edge id should not be found as a node");

        Optional<Node> notFound = graph.node(-999);
        assertFalse(notFound.isPresent(), "Non-existent node should return empty Optional");
    }

    @Test
    public void testGetEdgeById() {
        Optional<Edge> optE1 = graph.edge(e1.id());
        assertTrue(optE1.isPresent(), "Edge e1 should be found by id");
        assertEquals(e1.id(), optE1.get().id());

        Optional<Edge> optN1 = graph.edge(n1.id());
        assertFalse(optN1.isPresent(), "Node id should not be found as an edge");

        Optional<Edge> notFound = graph.edge(-999);
        assertFalse(notFound.isPresent(), "Non-existent edge should return empty Optional");
    }
}
