package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

/**
 * Validates algebraic properties of Graph operations (union, intersection, difference).
 * These properties ensure operations behave predictably as mathematical sets.
 */
public class GraphAlgebraicPropertiesTest {

    private EphemeralGraph gA, gB, gC;
    private Node a, b, c, d, e;
    private Edge ab, bc, cd, de;

    @BeforeEach
    public void setUp() {
        a = new EphemeralNode();
        b = new EphemeralNode();
        c = new EphemeralNode();
        d = new EphemeralNode();
        e = new EphemeralNode();

        ab = new EphemeralEdge(a, b);
        bc = new EphemeralEdge(b, c);
        cd = new EphemeralEdge(c, d);
        de = new EphemeralEdge(d, e);

        gA = new EphemeralGraph(a, b, c);
        gA.add(ab);
        gA.add(bc);

        gB = new EphemeralGraph(c, d);
        gB.add(cd);

        gC = new EphemeralGraph(a, b, d, e);
        gC.add(ab);
        gC.add(de);
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
        // A U B == B U A
        Graph aUnionB = gA.union(gB);
        Graph bUnionA = gB.union(gA);
        assertGraphsEqual(aUnionB, bUnionA);
    }

    @Test
    public void testIntersectionCommutativity() {
        // A ∩ B == B ∩ A
        Graph aIntersectB = gA.intersection(gB);
        Graph bIntersectA = gB.intersection(gA);
        assertGraphsEqual(aIntersectB, bIntersectA);
    }

    @Test
    public void testUnionAssociativity() {
        // (A U B) U C == A U (B U C)
        Graph aUnionB_UnionC = gA.union(gB).union(gC);
        Graph aUnion_BUnionC = gA.union(gB.union(gC));
        assertGraphsEqual(aUnionB_UnionC, aUnion_BUnionC);

        // Also varargs union: union(A, B, C)
        Graph unionAll = gA.union(gB, gC);
        assertGraphsEqual(aUnionB_UnionC, unionAll);
    }

    @Test
    public void testIntersectionAssociativity() {
        // (A ∩ B) ∩ C == A ∩ (B ∩ C)
        Graph aIntersectB_IntersectC = gA.intersection(gB).intersection(gC);
        Graph aIntersect_BIntersectC = gA.intersection(gB.intersection(gC));
        assertGraphsEqual(aIntersectB_IntersectC, aIntersect_BIntersectC);

        // Also varargs intersection: intersection(A, B, C)
        Graph intersectAll = gA.intersection(gB, gC);
        assertGraphsEqual(aIntersectB_IntersectC, intersectAll);
    }

    @Test
    public void testIdempotence() {
        // A U A == A
        Graph aUnionA = gA.union(gA);
        assertGraphsEqual(gA, aUnionA);

        // A ∩ A == A
        Graph aIntersectA = gA.intersection(gA);
        assertGraphsEqual(gA, aIntersectA);
    }

    @Test
    public void testIdentityOperations() {
        EphemeralGraph empty = new EphemeralGraph();

        // A U ∅ == A
        assertGraphsEqual(gA, gA.union(empty));
        assertGraphsEqual(gA, empty.union(gA));

        // A ∩ ∅ == ∅
        assertGraphsEqual(empty, gA.intersection(empty));
        assertGraphsEqual(empty, empty.intersection(gA));

        // A \ ∅ == A
        assertGraphsEqual(gA, gA.difference(empty));

        // ∅ \ A == ∅
        assertGraphsEqual(empty, empty.difference(gA));
    }

    @Test
    public void testDistributiveProperties() {
        // A ∩ (B U C) == (A ∩ B) U (A ∩ C)
        Graph aIntersect_BUnionC = gA.intersection(gB.union(gC));
        Graph aIntersectB_Union_aIntersectC = gA.intersection(gB).union(gA.intersection(gC));
        assertGraphsEqual(aIntersect_BUnionC, aIntersectB_Union_aIntersectC);

        // A U (B ∩ C) == (A U B) ∩ (A U C)
        Graph aUnion_BIntersectC = gA.union(gB.intersection(gC));
        Graph aUnionB_Intersect_aUnionC = gA.union(gB).intersection(gA.union(gC));
        assertGraphsEqual(aUnion_BIntersectC, aUnionB_Intersect_aUnionC);
    }
}
