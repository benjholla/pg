package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class IdentityLawsInvariantTest {

    private GlobalGraph gA;
    private GlobalGraph emptyGraph;

    @BeforeEach
    public void setUp() {
        GlobalNode a = new GlobalNode();
        GlobalNode b = new GlobalNode();
        GlobalEdge ab = new GlobalEdge(a, b);

        gA = new GlobalGraph(new GlobalNodeSet(a, b));
        gA.addEdge(ab);

        emptyGraph = new GlobalGraph();
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
