package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class IdempotenceInvariantTest {

    private GlobalGraph gA;

    @BeforeEach
    public void setUp() {
        GlobalNode a = new GlobalNode();
        GlobalNode b = new GlobalNode();
        GlobalEdge ab = new GlobalEdge(a, b);

        gA = new GlobalGraph(new GlobalNodeSet(a, b));
        gA.addEdge(ab);
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
    public void testUnionIdempotence() {
        // A U A == A
        Graph aUnionA = gA.union(gA);
        assertGraphsEqual(gA, aUnionA);
    }

    @Test
    public void testIntersectionIdempotence() {
        // A ∩ A == A
        Graph aIntersectA = gA.intersection(gA);
        assertGraphsEqual(gA, aIntersectA);
    }
}
