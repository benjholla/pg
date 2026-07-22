package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

/**
 * Validates invariant identity properties related to set differences on graphs.
 */
public class DifferenceIdentityInvariantTest {

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

        gA = new GlobalGraph(new GlobalNodeSet(a, b, c));
        gA.addEdge(ab);
        gA.addEdge(bc);

        gB = new GlobalGraph(new GlobalNodeSet(b, c, d));
        gB.addEdge(bc);
        gB.addEdge(cd);

        gC = new GlobalGraph(new GlobalNodeSet(a, d));
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
    public void testDifferenceIntersectionIdentity() {
        // A \ B == A \ (A ∩ B)
        Graph aMinusB = gA.difference(gB);
        Graph aIntB = gA.intersection(gB);
        Graph aMinusAIntB = gA.difference(aIntB);

        assertGraphsEqual(aMinusB, aMinusAIntB);
    }

    @Test
    public void testDifferenceEdgesIntersectionIdentity() {
        // A \_e B == A \_e (A ∩ B)
        Graph aMinusEdgesB = gA.differenceEdges(gB);
        Graph aIntB = gA.intersection(gB);
        Graph aMinusEdgesAIntB = gA.differenceEdges(aIntB);

        assertGraphsEqual(aMinusEdgesB, aMinusEdgesAIntB);
    }

    @Test
    public void testDifferenceUnionIdentity() {
        // (A \ B) \ C == A \ (B U C)
        Graph aMinusB = gA.difference(gB);
        Graph aMinusBMinusC = aMinusB.difference(gC);

        Graph bUnionC = gB.union(gC);
        Graph aMinusBUnionC = gA.difference(bUnionC);

        assertGraphsEqual(aMinusBMinusC, aMinusBUnionC);
    }

    @Test
    public void testDifferenceEdgesUnionIdentity() {
        // (A \_e B) \_e C == A \_e (B U C)
        Graph aMinusEdgesB = gA.differenceEdges(gB);
        Graph aMinusEdgesBMinusEdgesC = aMinusEdgesB.differenceEdges(gC);

        Graph bUnionC = gB.union(gC);
        Graph aMinusEdgesBUnionC = gA.differenceEdges(bUnionC);

        assertGraphsEqual(aMinusEdgesBMinusEdgesC, aMinusEdgesBUnionC);
    }
}
