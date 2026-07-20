package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

/**
 * Validates properties related to differenceEdges operations on graphs.
 */
public class DifferenceEdgesPropertiesInvariantTest {

    private GlobalGraph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        GlobalNode a = new GlobalNode();
        GlobalNode b = new GlobalNode();
        GlobalNode c = new GlobalNode();

        GlobalEdge ab = new GlobalEdge(a, b);
        GlobalEdge bc = new GlobalEdge(b, c);

        gA = new GlobalGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);

        gB = new GlobalGraph(a, b);
        gB.addEdge(ab);

        gC = new GlobalGraph(b, c);
        gC.addEdge(bc);
    }

    private void assertGraphsEqual(Graph expected, Graph actual) {
        assertEquals(expected.nodes().size(), actual.nodes().size(), "Node count mismatch");
        assertEquals(expected.edges().size(), actual.edges().size(), "Edge count mismatch");
        assertTrue(expected.nodes().containsAll(actual.nodes()), "Nodes mismatch");
        assertTrue(actual.nodes().containsAll(expected.nodes()), "Nodes mismatch");
        assertTrue(expected.edges().containsAll(actual.edges()), "Edges mismatch");
        assertTrue(actual.edges().containsAll(expected.edges()), "Edges mismatch");
    }

    @Test
    public void testDifferenceEdgesFromEmpty() {
        // ∅ \_e A = ∅
        GlobalGraph empty = new GlobalGraph();
        Graph diff = empty.differenceEdges(gA);
        assertTrue(diff.nodes().isEmpty());
    }

    @Test
    public void testDifferenceEdgesOfEmpty() {
        // A \_e ∅ = A
        GlobalGraph empty = new GlobalGraph();
        Graph diff = gA.differenceEdges(empty);
        assertGraphsEqual(gA, diff);
    }

    @Test
    public void testDifferenceEdgesSelf() {
        // A \_e A = Nodes(A)  (All edges removed, all nodes remain)
        Graph diff = gA.differenceEdges(gA);
        assertEquals(gA.nodes().size(), diff.nodes().size(), "differenceEdges should not remove nodes");
        assertTrue(diff.nodes().containsAll(gA.nodes()), "differenceEdges should retain all nodes");
        assertEquals(0, diff.edges().size(), "differenceEdges self should remove all edges");
    }

    @Test
    public void testDifferenceEdgesDisjoint() {
        // A \_e B = A (if A and B have no edges in common)
        GlobalNode d = new GlobalNode();
        GlobalNode e = new GlobalNode();
        GlobalEdge de = new GlobalEdge(d, e);
        GlobalGraph gDisjoint = new GlobalGraph(d, e);
        gDisjoint.addEdge(de);

        Graph diff = gA.differenceEdges(gDisjoint);
        assertGraphsEqual(gA, diff);
    }

    @Test
    public void testDifferenceEdgesSubset() {
        // A \_e B should strictly remove the edges present in B
        Graph diff = gA.differenceEdges(gB);
        assertEquals(gA.nodes().size(), diff.nodes().size(), "differenceEdges should not remove nodes");
        assertEquals(1, diff.edges().size(), "One edge should remain");
        assertTrue(diff.edges().containsAll(gC.edges()), "The remaining edge should be the one not in B");
    }
}
