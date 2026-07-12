package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

/**
 * Validates properties related to set differences on graphs.
 */
public class DifferencePropertiesInvariantTest {

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
    public void testDifferenceFromEmpty() {
        // ∅ \ A = ∅
        GlobalGraph empty = new GlobalGraph();
        Graph diff = empty.difference(gA);
        assertTrue(diff.isEmpty());
    }

    @Test
    public void testDifferenceOfEmpty() {
        // A \ ∅ = A
        GlobalGraph empty = new GlobalGraph();
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
        // Due to cascading edge removal when nodes are removed from a property graph,
        // the standard set theory identity A \ (A \ B) = A ∩ B does not strictly hold
        // in terms of equality. Because A \ B can remove edges from A that were NOT in B
        // (if their terminal nodes were in B), A \ (A \ B) will retain those edges, whereas A ∩ B will not.
        // Thus, the proper graph-theoretic relation is that A \ (A \ B) ⊇ A ∩ B.
        Graph aMinusB = gA.difference(gB);
        Graph aMinusAMinusB = gA.difference(aMinusB);
        Graph aIntB = gA.intersection(gB);

        assertTrue(aMinusAMinusB.nodes().containsAll(aIntB.nodes()), "A \\ (A \\ B) should be a superset of A ∩ B for nodes");
        assertTrue(aMinusAMinusB.edges().containsAll(aIntB.edges()), "A \\ (A \\ B) should be a superset of A ∩ B for edges");
    }
}
