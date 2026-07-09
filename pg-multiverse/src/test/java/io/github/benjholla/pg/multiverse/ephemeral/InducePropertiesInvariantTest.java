package io.github.benjholla.pg.multiverse.ephemeral;

import io.github.benjholla.pg.api.Edge;

import io.github.benjholla.pg.api.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates properties related to graph inducement.
 */
public class InducePropertiesInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph gA;
    private Node a, b, c;
    private Edge ab, bc;

    @BeforeEach
    public void setUp() {
        a = factory.createNode();
        b = factory.createNode();
        c = factory.createNode();

        ab = factory.createEdge(a, b);
        bc = factory.createEdge(b, c);

        gA = factory.createGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);
    }

    @Test
    public void testInduceEmptyGraph() {
        Graph empty = factory.createGraph();
        Graph induced = gA.induce(empty);

        assertEquals(gA.nodes().size(), induced.nodes().size());
        assertEquals(gA.edges().size(), induced.edges().size());
    }

    @Test
    public void testInduceEmptyEdgeSet() {
        EphemeralEdgeSet emptyEdges = new EphemeralEdgeSet();
        Graph induced = gA.induce(emptyEdges);

        assertEquals(gA.nodes().size(), induced.nodes().size());
        assertEquals(gA.edges().size(), induced.edges().size());
    }

    @Test
    public void testInduceAddsNewValidEdges() {
        Graph nodesOnly = factory.createGraph(a, b, c);
        EphemeralEdgeSet edgesToAdd = new EphemeralEdgeSet();
        edgesToAdd.add(ab);

        Graph induced = nodesOnly.induce(edgesToAdd);

        assertEquals(3, induced.nodes().size());
        assertEquals(1, induced.edges().size());
        assertTrue(induced.edges().contains(ab));
    }
}
