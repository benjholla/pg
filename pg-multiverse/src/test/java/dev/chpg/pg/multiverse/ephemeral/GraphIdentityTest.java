package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphIdentityTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph graph;
    private Node n1, n2;
    private Edge e1;

    @BeforeEach
    public void setUp() {
        graph = factory.createGraph();
        n1 = factory.createNode();
        n2 = factory.createNode();
        e1 = factory.createEdge(n1, n2);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);
    }

    @Test
    public void testGetNodeById() {
        Optional<Node> optN1 = graph.node(n1.id());
        assertTrue(optN1.isPresent(), "Node n1 should be found by id");
        assertEquals(n1.id(), optN1.get().id());

        // Node and Edge IDs can overlap in EphemeralGraph, so we can't test that e1.id() doesn't exist as a node

        Optional<Node> notFound = graph.node(-999);
        assertFalse(notFound.isPresent(), "Non-existent node should return empty Optional");
    }

    @Test
    public void testGetEdgeById() {
        Optional<Edge> optE1 = graph.edge(e1.id());
        assertTrue(optE1.isPresent(), "Edge e1 should be found by id");
        assertEquals(e1.id(), optE1.get().id());

        // Node and Edge IDs can overlap in EphemeralGraph, so we can't test that n1.id() doesn't exist as an edge

        Optional<Edge> notFound = graph.edge(-999);
        assertFalse(notFound.isPresent(), "Non-existent edge should return empty Optional");
    }
}
