package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class UnionIdentityInvariantTest {

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
    public void testUnionCommutativity() {
        // A U B == B U A
        Graph aUnionB = gA.union(gB);
        Graph bUnionA = gB.union(gA);

        assertGraphsEqual(aUnionB, bUnionA);
    }

    @Test
    public void testUnionAssociativity() {
        // (A U B) U C == A U (B U C)
        Graph aUnionBUnionC = (gA.union(gB)).union(gC);
        Graph aUnionBUnionC2 = gA.union(gB.union(gC));

        assertGraphsEqual(aUnionBUnionC, aUnionBUnionC2);
    }

    @Test
    public void testUnionIdempotence() {
        // A U A == A
        Graph aUnionA = gA.union(gA);

        assertGraphsEqual(gA, aUnionA);
    }

    @Test
    public void testUnionNodeIdentity() {
        // A U n == A U {n}
        GlobalNode n = new GlobalNode();
        Graph aUnionNode = gA.union(n);
        Graph aUnionGraphNode = gA.union(new GlobalGraph(n));

        assertGraphsEqual(aUnionNode, aUnionGraphNode);
    }

    @Test
    public void testUnionEdgeIdentity() {
        // A U e == A U {e}
        GlobalNode n1 = new GlobalNode();
        GlobalNode n2 = new GlobalNode();
        GlobalEdge e = new GlobalEdge(n1, n2);

        Graph aUnionEdge = gA.union(e);

        GlobalGraph gE = new GlobalGraph();
        gE.addEdge(e); // Auto adds terminal nodes
        Graph aUnionGraphEdge = gA.union(gE);

        assertGraphsEqual(aUnionEdge, aUnionGraphEdge);
    }
}
