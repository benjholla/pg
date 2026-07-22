package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class IdempotenceInvariantTest {

    private static final EphemeralFactory factory = new EphemeralGraph().factory();
    private Graph gA;

    @BeforeEach
    public void setUp() {
        Node a = factory.createNode();
        Node b = factory.createNode();
        Edge ab = factory.createEdge(a, b);

        gA = factory.createGraph(new EphemeralNodeSet(a, b));
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
