package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class AssociativityLawsInvariantTest {

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
    public void testUnionAssociativity() {
        // (A U B) U C == A U (B U C)
        Graph aUnionB_UnionC = gA.union(gB).union(gC);
        Graph aUnion_BUnionC = gA.union(gB.union(gC));
        assertGraphsEqual(aUnionB_UnionC, aUnion_BUnionC);

        // Varargs union: A.union(B, C)
        Graph unionAll = gA.union(gB, gC);
        assertGraphsEqual(aUnionB_UnionC, unionAll);
    }

    @Test
    public void testIntersectionAssociativity() {
        // (A ∩ B) ∩ C == A ∩ (B ∩ C)
        Graph aIntersectB_IntersectC = gA.intersection(gB).intersection(gC);
        Graph aIntersect_BIntersectC = gA.intersection(gB.intersection(gC));
        assertGraphsEqual(aIntersectB_IntersectC, aIntersect_BIntersectC);

        // Varargs intersection: A.intersection(B, C)
        Graph intersectAll = gA.intersection(gB, gC);
        assertGraphsEqual(aIntersectB_IntersectC, intersectAll);
    }
}
