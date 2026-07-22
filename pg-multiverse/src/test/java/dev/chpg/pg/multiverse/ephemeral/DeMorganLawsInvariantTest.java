package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

/**
 * Validates De Morgan's laws for set operations on graphs:
 * 1. A \ (B U C) = (A \ B) ∩ (A \ C)
 * 2. A \ (B ∩ C) = (A \ B) U (A \ C)
 */
public class DeMorganLawsInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        Node a = factory.createNode();
        Node b = factory.createNode();
        Node c = factory.createNode();

        Edge ab = factory.createEdge(a, b);
        Edge bc = factory.createEdge(b, c);

        // A is the superset
        gA = factory.createGraph(new EphemeralNodeSet(a, b, c));
        gA.addEdge(ab);
        gA.addEdge(bc);

        // B and C are subsets
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
