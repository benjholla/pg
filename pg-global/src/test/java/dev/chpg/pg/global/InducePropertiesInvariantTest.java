package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

/**
 * Validates properties related to graph inducement.
 */
public class InducePropertiesInvariantTest {

    private GlobalGraph gA;
    private GlobalNode a, b, c;
    private GlobalEdge ab, bc;

    @BeforeEach
    public void setUp() {
        a = new GlobalNode();
        b = new GlobalNode();
        c = new GlobalNode();

        ab = new GlobalEdge(a, b);
        bc = new GlobalEdge(b, c);

        gA = new GlobalGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);
    }

    @Test
    public void testInduceEmptyGraph() {
        GlobalGraph empty = new GlobalGraph();
        Graph induced = gA.induce(empty);

        // As currently implemented, induce(edges) starts with a copy of 'this'
        // (which includes existing nodes and edges), and then adds any additional
        // valid edges from the given edge set.
        assertEquals(gA.nodes().size(), induced.nodes().size());
        assertEquals(gA.edges().size(), induced.edges().size());
    }

    @Test
    public void testInduceEmptyEdgeSet() {
        GlobalEdgeSet emptyEdges = new GlobalEdgeSet();
        Graph induced = gA.induce(emptyEdges);

        assertEquals(gA.nodes().size(), induced.nodes().size());
        assertEquals(gA.edges().size(), induced.edges().size());
    }

    @Test
    public void testInduceAddsNewValidEdges() {
        GlobalGraph nodesOnly = new GlobalGraph(a, b, c);
        GlobalEdgeSet edgesToAdd = new GlobalEdgeSet();
        edgesToAdd.add(ab);

        Graph induced = nodesOnly.induce(edgesToAdd);

        assertEquals(3, induced.nodes().size());
        assertEquals(1, induced.edges().size());
        assertTrue(induced.edges().contains(ab));
    }
}
