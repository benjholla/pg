package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.Universe;

public class IdentityLawsInvariantTest {

    private Graph gA;
    private Graph emptyGraph;
    private EphemeralFactory factory;

    @BeforeEach
    public void setUp() {
        Universe universe = new Universe();
        factory = new EphemeralGraph(universe).factory();

        Node a = factory.createNode();
        Node b = factory.createNode();
        Edge ab = factory.createEdge(a, b);

        gA = factory.createGraph(a, b);
        gA.addEdge(ab);

        emptyGraph = factory.createGraph();
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
    public void testUnionIdentity() {
        // A U ∅ = A
        Graph aUnionEmpty = gA.union(emptyGraph);
        assertGraphsEqual(gA, aUnionEmpty);

        // ∅ U A = A
        Graph emptyUnionA = emptyGraph.union(gA);
        assertGraphsEqual(gA, emptyUnionA);
    }

    @Test
    public void testIntersectionIdentity() {
        // A ∩ ∅ = ∅
        Graph aIntersectEmpty = gA.intersection(emptyGraph);
        assertGraphsEqual(emptyGraph, aIntersectEmpty);

        // ∅ ∩ A = ∅
        Graph emptyIntersectA = emptyGraph.intersection(gA);
        assertGraphsEqual(emptyGraph, emptyIntersectA);
    }

    @Test
    public void testDifferenceIdentity() {
        // A \ ∅ = A
        Graph aMinusEmpty = gA.difference(emptyGraph);
        assertGraphsEqual(gA, aMinusEmpty);

        // ∅ \ A = ∅
        Graph emptyMinusA = emptyGraph.difference(gA);
        assertGraphsEqual(emptyGraph, emptyMinusA);

        // A \ A = ∅
        Graph aMinusA = gA.difference(gA);
        assertGraphsEqual(emptyGraph, aMinusA);
    }
}
