package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates De Morgan's laws for set operations on graphs:
 * 1. A \ (B U C) = (A \ B) ∩ (A \ C)
 * 2. A \ (B ∩ C) = (A \ B) U (A \ C)
 */
public class DeMorganLawsInvariantTest {

    private EphemeralGraph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        EphemeralNode a = new EphemeralNode();
        EphemeralNode b = new EphemeralNode();
        EphemeralNode c = new EphemeralNode();

        EphemeralEdge ab = new EphemeralEdge(a, b);
        EphemeralEdge bc = new EphemeralEdge(b, c);

        // A is the superset
        gA = new EphemeralGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);

        // B and C are subsets
        gB = new EphemeralGraph(a, b);
        gB.addEdge(ab);

        gC = new EphemeralGraph(b, c);
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
    public void testDeMorganLaw1() {
        // A \ (B U C) == (A \ B) ∩ (A \ C)
        Graph aMinusBuC = gA.difference(gB.union(gC));

        Graph aMinusB = gA.difference(gB);
        Graph aMinusC = gA.difference(gC);
        Graph aMinusBIntAMinusC = aMinusB.intersection(aMinusC);

        assertGraphsEqual(aMinusBuC, aMinusBIntAMinusC);
    }

    @Test
    public void testDeMorganLaw2() {
        // A \ (B ∩ C) == (A \ B) U (A \ C)
        Graph bIntC = gB.intersection(gC);
        Graph aMinusBIntC = gA.difference(bIntC);

        Graph aMinusB = gA.difference(gB);
        Graph aMinusC = gA.difference(gC);
        Graph aMinusBUnionAMinusC = aMinusB.union(aMinusC);

        assertGraphsEqual(aMinusBIntC, aMinusBUnionAMinusC);
    }
}
