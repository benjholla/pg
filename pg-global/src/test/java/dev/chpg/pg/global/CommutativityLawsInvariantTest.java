package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class CommutativityLawsInvariantTest {

    private GlobalGraph gA;
    private GlobalGraph gB;

    @BeforeEach
    public void setUp() {
        GlobalNode a = new GlobalNode();
        GlobalNode b = new GlobalNode();
        GlobalNode c = new GlobalNode();

        GlobalEdge ab = new GlobalEdge(a, b);
        GlobalEdge bc = new GlobalEdge(b, c);

        gA = new GlobalGraph(new GlobalNodeSet(a, b));
        gA.addEdge(ab);

        gB = new GlobalGraph(new GlobalNodeSet(b, c));
        gB.addEdge(bc);
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
    public void testUnionCommutativity() {
        // A U B = B U A
        Graph aUnionB = gA.union(gB);
        Graph bUnionA = gB.union(gA);
        assertGraphsEqual(aUnionB, bUnionA);
    }

    @Test
    public void testIntersectionCommutativity() {
        // A ∩ B = B ∩ A
        Graph aIntersectB = gA.intersection(gB);
        Graph bIntersectA = gB.intersection(gA);
        assertGraphsEqual(aIntersectB, bIntersectA);
    }
}
