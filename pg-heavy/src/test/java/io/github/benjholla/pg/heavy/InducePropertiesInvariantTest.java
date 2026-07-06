package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates properties related to graph inducement.
 */
public class InducePropertiesInvariantTest {

    private HeavyGraph gA;
    private HeavyNode a, b, c;
    private HeavyEdge ab, bc;

    @BeforeEach
    public void setUp() {
        a = new HeavyNode();
        b = new HeavyNode();
        c = new HeavyNode();

        ab = new HeavyEdge(a, b);
        bc = new HeavyEdge(b, c);

        gA = new HeavyGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);
    }

    @Test
    public void testInduceEmptyGraph() {
        HeavyGraph empty = new HeavyGraph();
        Graph induced = gA.induce(empty);

        // As currently implemented, induce(edges) starts with a copy of 'this'
        // (which includes existing nodes and edges), and then adds any additional
        // valid edges from the given edge set.
        assertEquals(gA.nodes().size(), induced.nodes().size());
        assertEquals(gA.edges().size(), induced.edges().size());
    }

    @Test
    public void testInduceEmptyEdgeSet() {
        HeavyEdgeSet emptyEdges = new HeavyEdgeSet();
        Graph induced = gA.induce(emptyEdges);

        assertEquals(gA.nodes().size(), induced.nodes().size());
        assertEquals(gA.edges().size(), induced.edges().size());
    }

    @Test
    public void testInduceAddsNewValidEdges() {
        HeavyGraph nodesOnly = new HeavyGraph(a, b, c);
        HeavyEdgeSet edgesToAdd = new HeavyEdgeSet();
        edgesToAdd.add(ab);

        Graph induced = nodesOnly.induce(edgesToAdd);

        assertEquals(3, induced.nodes().size());
        assertEquals(1, induced.edges().size());
        assertTrue(induced.edges().contains(ab));
    }
}
