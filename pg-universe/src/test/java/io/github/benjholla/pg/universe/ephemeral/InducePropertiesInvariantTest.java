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

    private EphemeralGraph gA;
    private EphemeralNode a, b, c;
    private EphemeralEdge ab, bc;

    @BeforeEach
    public void setUp() {
        a = new EphemeralNode();
        b = new EphemeralNode();
        c = new EphemeralNode();

        ab = new EphemeralEdge(a, b);
        bc = new EphemeralEdge(b, c);

        gA = new EphemeralGraph(a, b, c);
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
        EphemeralGraph nodesOnly = new EphemeralGraph(a, b, c);
        EphemeralEdgeSet edgesToAdd = new EphemeralEdgeSet();
        edgesToAdd.add(ab);

        Graph induced = nodesOnly.induce(edgesToAdd);

        assertEquals(3, induced.nodes().size());
        assertEquals(1, induced.edges().size());
        assertTrue(induced.edges().contains(ab));
    }
}
