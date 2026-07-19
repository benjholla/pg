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

        // union: A.union(B).union(C)
        Graph unionAll = gA.union(gB).union(gC);
        assertGraphsEqual(aUnionB_UnionC, unionAll);

        // Node union
        GlobalNode[] gBnodes = gB.nodes().stream().toArray(GlobalNode[]::new);
        GlobalNode[] gCnodes = gC.nodes().stream().toArray(GlobalNode[]::new);
        GlobalNode[] gB_and_gC_nodes = new GlobalNode[gBnodes.length + gCnodes.length];
        System.arraycopy(gBnodes, 0, gB_and_gC_nodes, 0, gBnodes.length);
        System.arraycopy(gCnodes, 0, gB_and_gC_nodes, gBnodes.length, gCnodes.length);

        Graph chainedUnionNodes = gA.union(gBnodes).union(gCnodes);
        assertGraphsEqual(chainedUnionNodes, gA.union(gB_and_gC_nodes));

        // Edge union
        GlobalEdge[] gBedges = gB.edges().stream().toArray(GlobalEdge[]::new);
        GlobalEdge[] gCedges = gC.edges().stream().toArray(GlobalEdge[]::new);
        GlobalEdge[] gB_and_gC_edges = new GlobalEdge[gBedges.length + gCedges.length];
        System.arraycopy(gBedges, 0, gB_and_gC_edges, 0, gBedges.length);
        System.arraycopy(gCedges, 0, gB_and_gC_edges, gBedges.length, gCedges.length);

        Graph chainedUnionEdges = gA.union(gBedges).union(gCedges);
        assertGraphsEqual(chainedUnionEdges, gA.union(gB_and_gC_edges));
    }

    @Test
    public void testIntersectionAssociativity() {
        // (A ∩ B) ∩ C == A ∩ (B ∩ C)
        Graph aIntersectB_IntersectC = gA.intersection(gB).intersection(gC);
        Graph aIntersect_BIntersectC = gA.intersection(gB.intersection(gC));
        assertGraphsEqual(aIntersectB_IntersectC, aIntersect_BIntersectC);

        // intersection: A.intersection(B).intersection(C)
        Graph intersectAll = gA.intersection(gB).intersection(gC);
        assertGraphsEqual(aIntersectB_IntersectC, intersectAll);

        GlobalNode[] gBnodes = gB.nodes().stream().toArray(GlobalNode[]::new);
        GlobalNode[] gCnodes = gC.nodes().stream().toArray(GlobalNode[]::new);

        GlobalNode[] gB_intersect_gC_nodes = gB.intersection(gC).nodes().stream().toArray(GlobalNode[]::new);
        assertGraphsEqual(gA.intersection(gB_intersect_gC_nodes), gA.intersection(gBnodes).intersection(gCnodes));

        GlobalEdge[] gBedges = gB.edges().stream().toArray(GlobalEdge[]::new);
        GlobalEdge[] gCedges = gC.edges().stream().toArray(GlobalEdge[]::new);

        GlobalEdge[] gB_intersect_gC_edges = gB.intersection(gC).edges().stream().toArray(GlobalEdge[]::new);
        assertGraphsEqual(gA.intersection(gB_intersect_gC_edges), gA.intersection(gBedges).intersection(gCedges));
    }
}
