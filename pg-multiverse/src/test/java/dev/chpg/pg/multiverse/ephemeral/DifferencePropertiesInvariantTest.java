package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

/**
 * Validates properties related to set differences on graphs.
 */
public class DifferencePropertiesInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        Node a = factory.createNode();
        Node b = factory.createNode();
        Node c = factory.createNode();

        Edge ab = factory.createEdge(a, b);
        Edge bc = factory.createEdge(b, c);

        gA = factory.createGraph(new EphemeralNodeSet(a, b, c));
        gA.addEdge(ab);
        gA.addEdge(bc);

        gB = factory.createGraph(new EphemeralNodeSet(a, b));
        gB.addEdge(ab);

        gC = factory.createGraph(new EphemeralNodeSet(b, c));
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
    public void testDifferenceFromEmpty() {
        // ∅ \ A = ∅
        Graph empty = factory.createGraph();
        Graph diff = empty.difference(gA);
        assertTrue(diff.nodes().isEmpty());
    }

    @Test
    public void testDifferenceOfEmpty() {
        // A \ ∅ = A
        Graph empty = factory.createGraph();
        Graph diff = gA.difference(empty);
        assertGraphsEqual(gA, diff);
    }

    @Test
    public void testDifferenceSelf() {
        // A \ A = ∅
        Graph diff = gA.difference(gA);
        assertTrue(diff.nodes().isEmpty());
    }

    @Test
    public void testDifferenceIntersection() {
        // A \ (A \ B) = A ∩ B
        Graph aMinusB = gA.difference(gB);
        Graph aMinusAMinusB = gA.difference(aMinusB);
        Graph aIntB = gA.intersection(gB);

        assertGraphsEqual(aIntB, aMinusAMinusB);
    }
}
