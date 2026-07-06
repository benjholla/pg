package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates properties related to graph inducement.
 */
public class InducePropertiesInvariantTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    private EphemeralGraph gA;
    private EphemeralNode a, b, c;
    private EphemeralEdge ab, bc;

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
        EphemeralGraph empty = new EphemeralGraph();
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
        EphemeralGraph nodesOnly = factory.createGraph(a, b, c);
        EphemeralEdgeSet edgesToAdd = new EphemeralEdgeSet();
        edgesToAdd.add(ab);

        Graph induced = nodesOnly.induce(edgesToAdd);

        assertEquals(3, induced.nodes().size());
        assertEquals(1, induced.edges().size());
        assertTrue(induced.edges().contains(ab));
    }
}
