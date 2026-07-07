package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates properties related to set differences on graphs.
 */
public class DifferencePropertiesInvariantTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    private EphemeralGraph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        EphemeralNode a = factory.createNode();
        EphemeralNode b = factory.createNode();
        EphemeralNode c = factory.createNode();

        EphemeralEdge ab = factory.createEdge(a, b);
        EphemeralEdge bc = factory.createEdge(b, c);

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
    public void testDifferenceFromEmpty() {
        // ∅ \ A = ∅
        EphemeralGraph empty = new EphemeralGraph();
        Graph diff = empty.difference(gA);
        assertTrue(diff.isEmpty());
    }

    @Test
    public void testDifferenceOfEmpty() {
        // A \ ∅ = A
        EphemeralGraph empty = new EphemeralGraph();
        Graph diff = gA.difference(empty);
        assertGraphsEqual(gA, diff);
    }

    @Test
    public void testDifferenceSelf() {
        // A \ A = ∅
        Graph diff = gA.difference(gA);
        assertTrue(diff.isEmpty());
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
