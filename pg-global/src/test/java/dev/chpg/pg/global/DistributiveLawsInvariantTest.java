package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class DistributiveLawsInvariantTest {

    private GlobalGraph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        GlobalNode a = new GlobalNode();
        GlobalNode b = new GlobalNode();
        GlobalNode c = new GlobalNode();
        GlobalNode d = new GlobalNode();

        GlobalEdge ab = new GlobalEdge(a, b);
        GlobalEdge bc = new GlobalEdge(b, c);
        GlobalEdge cd = new GlobalEdge(c, d);

        gA = new GlobalGraph(a, b);
        gA.addEdge(ab);

        gB = new GlobalGraph(b, c);
        gB.addEdge(bc);

        gC = new GlobalGraph(c, d);
        gC.addEdge(cd);
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
    public void testIntersectionDistributesOverUnion() {
        // A ∩ (B U C) = (A ∩ B) U (A ∩ C)
        Graph bUnionC = gB.union(gC);
        Graph aIntersect_bUnionC = gA.intersection(bUnionC);

        Graph aIntersectB = gA.intersection(gB);
        Graph aIntersectC = gA.intersection(gC);
        Graph aIntersectB_Union_aIntersectC = aIntersectB.union(aIntersectC);

        assertGraphsEqual(aIntersect_bUnionC, aIntersectB_Union_aIntersectC);
    }

    @Test
    public void testUnionDistributesOverIntersection() {
        // A U (B ∩ C) = (A U B) ∩ (A U C)
        Graph bIntersectC = gB.intersection(gC);
        Graph aUnion_bIntersectC = gA.union(bIntersectC);

        Graph aUnionB = gA.union(gB);
        Graph aUnionC = gA.union(gC);
        Graph aUnionB_Intersect_aUnionC = aUnionB.intersection(aUnionC);

        assertGraphsEqual(aUnion_bIntersectC, aUnionB_Intersect_aUnionC);
    }
}
