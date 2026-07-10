package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates De Morgan's laws for set operations on graphs:
 * 1. A \ (B U C) = (A \ B) ∩ (A \ C)
 * 2. A \ (B ∩ C) ⊇ (A \ B) U (A \ C)
 *
 * Note: The second law is technically an equality for pure sets, but due to
 * cascading edge removals on property graphs (when an edge's node is removed),
 * the equality becomes a superset relationship for edges.
 */
public class DeMorganLawsInvariantTest {

    private GlobalGraph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        GlobalNode a = new GlobalNode();
        GlobalNode b = new GlobalNode();
        GlobalNode c = new GlobalNode();

        GlobalEdge ab = new GlobalEdge(a, b);
        GlobalEdge bc = new GlobalEdge(b, c);

        // A is the superset
        gA = new GlobalGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);

        // B and C are subsets
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
        // A \ (B ∩ C) ⊇ (A \ B) U (A \ C)
        // Due to cascading edge deletion, A \ (B ∩ C) might retain more edges than
        // the union of (A \ B) and (A \ C).
        Graph bIntC = gB.intersection(gC);
        Graph aMinusBIntC = gA.difference(bIntC);

        Graph aMinusB = gA.difference(gB);
        Graph aMinusC = gA.difference(gC);
        Graph aMinusBUnionAMinusC = aMinusB.union(aMinusC);

        assertTrue(aMinusBIntC.nodes().containsAll(aMinusBUnionAMinusC.nodes()), "Nodes should be a superset");
        assertTrue(aMinusBIntC.edges().containsAll(aMinusBUnionAMinusC.edges()), "Edges should be a superset");
    }
}
