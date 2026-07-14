package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

/**
 * Validates properties related to differenceEdges operations on graphs.
 */
public class DifferenceEdgesPropertiesInvariantTest {

    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    private Graph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        Node a = factory.createNode();
        Node b = factory.createNode();
        Node c = factory.createNode();

        Edge ab = factory.createEdge(a, b);
        Edge bc = factory.createEdge(b, c);

        gA = factory.createGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);

        gB = factory.createGraph(a, b);
        gB.addEdge(ab);

        gC = factory.createGraph(b, c);
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
        Graph empty = factory.createGraph();
        Graph diff = empty.differenceEdges(gA);
        assertTrue(diff.isEmpty());
    }

    @Test
    public void testDifferenceEdgesOfEmpty() {
        // A \_e ∅ = A
        Graph empty = factory.createGraph();
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
        Node d = factory.createNode();
        Node e = factory.createNode();
        Edge de = factory.createEdge(d, e);
        Graph gDisjoint = factory.createGraph(d, e);
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
