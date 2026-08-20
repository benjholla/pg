package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class IntersectionIdentityInvariantTest {

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
    public void testIntersectionCommutativity() {
        // A ∩ B == B ∩ A
        Graph aIntB = gA.intersection(gB);
        Graph bIntA = gB.intersection(gA);

        assertGraphsEqual(aIntB, bIntA);
    }

    @Test
    public void testIntersectionAssociativity() {
        // (A ∩ B) ∩ C == A ∩ (B ∩ C)
        Graph aIntBIntC = (gA.intersection(gB)).intersection(gC);
        Graph aIntBIntC2 = gA.intersection(gB.intersection(gC));

        assertGraphsEqual(aIntBIntC, aIntBIntC2);
    }

    @Test
    public void testIntersectionIdempotence() {
        // A ∩ A == A
        Graph aIntA = gA.intersection(gA);

        assertGraphsEqual(gA, aIntA);
    }

    @Test
    public void testIntersectionNodeIdentity() {
        // A ∩ n == A ∩ {n}
        GlobalNode a = (GlobalNode) gA.nodes().one().get();
        Graph aIntNode = gA.intersection(a);
        Graph aIntGraphNode = gA.intersection(new GlobalGraph(a));

        assertGraphsEqual(aIntNode, aIntGraphNode);
    }

    @Test
    public void testIntersectionEdgeIdentity() {
        // A ∩ e == A ∩ {e} (not exactly because edge intersection requires retaining terminal nodes if in A)
        // Let's test the equivalence. API contract for intersection(Edge): retains terminal nodes IF they existed in original graph.
        GlobalEdge e = (GlobalEdge) gA.edges().one().get();
        Graph aIntEdge = gA.intersection(e);

        GlobalGraph gE = new GlobalGraph();
        gE.addEdge(e); // This auto-adds terminal nodes to gE
        Graph aIntGraphEdge = gA.intersection(gE);

        assertGraphsEqual(aIntEdge, aIntGraphEdge);
    }
}
