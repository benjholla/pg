package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates properties related to set differences on graphs.
 */
public class DifferencePropertiesInvariantTest {

    private HeavyGraph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        HeavyNode a = (HeavyNode) new HeavyGraph().createNode();
        HeavyNode b = (HeavyNode) new HeavyGraph().createNode();
        HeavyNode c = (HeavyNode) new HeavyGraph().createNode();

        HeavyEdge ab = (HeavyEdge) new HeavyGraph().createEdge(a, b);
        HeavyEdge bc = (HeavyEdge) new HeavyGraph().createEdge(b, c);

        gA = (HeavyGraph) new HeavyGraph().createGraph(a, b, c);
        gA.addEdge(ab);
        gA.addEdge(bc);

        gB = (HeavyGraph) new HeavyGraph().createGraph(a, b);
        gB.addEdge(ab);

        gC = (HeavyGraph) new HeavyGraph().createGraph(b, c);
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
        HeavyGraph empty = new HeavyGraph();
        Graph diff = empty.difference(gA);
        assertTrue(diff.isEmpty());
    }

    @Test
    public void testDifferenceOfEmpty() {
        // A \ ∅ = A
        HeavyGraph empty = new HeavyGraph();
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
