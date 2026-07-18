package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class AssociativityLawsInvariantTest {

    private static final EphemeralFactory factory = new EphemeralGraph().factory();
    private Graph gA, gB, gC;

    @BeforeEach
    public void setUp() {
        Node a = factory.createNode();
        Node b = factory.createNode();
        Node c = factory.createNode();
        Node d = factory.createNode();

        Edge ab = factory.createEdge(a, b);
        Edge bc = factory.createEdge(b, c);
        Edge cd = factory.createEdge(c, d);

        gA = factory.createGraph(a, b);
        gA.addEdge(ab);

        gB = factory.createGraph(b, c);
        gB.addEdge(bc);

        gC = factory.createGraph(c, d);
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

        // Varargs Node union
        Node[] gBnodes = gB.nodes().stream().toArray(Node[]::new);
        Node[] gCnodes = gC.nodes().stream().toArray(Node[]::new);
        Node[] gB_and_gC_nodes = new Node[gBnodes.length + gCnodes.length];
        System.arraycopy(gBnodes, 0, gB_and_gC_nodes, 0, gBnodes.length);
        System.arraycopy(gCnodes, 0, gB_and_gC_nodes, gBnodes.length, gCnodes.length);

        Graph chainedUnionNodes = gA.union(gBnodes).union(gCnodes);
        assertGraphsEqual(chainedUnionNodes, gA.union(gB_and_gC_nodes));

        // Varargs Edge union
        Edge[] gBedges = gB.edges().stream().toArray(Edge[]::new);
        Edge[] gCedges = gC.edges().stream().toArray(Edge[]::new);
        Edge[] gB_and_gC_edges = new Edge[gBedges.length + gCedges.length];
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

        // Varargs intersection: A.intersection(B, C)
        Graph intersectAll = gA.intersection(gB, gC);
        assertGraphsEqual(aIntersectB_IntersectC, intersectAll);

        Node[] gBnodes = gB.nodes().stream().toArray(Node[]::new);
        Node[] gCnodes = gC.nodes().stream().toArray(Node[]::new);

        Node[] gB_intersect_gC_nodes = gB.intersection(gC).nodes().stream().toArray(Node[]::new);
        assertGraphsEqual(gA.intersection(gB_intersect_gC_nodes), gA.intersection(gBnodes).intersection(gCnodes));

        Edge[] gBedges = gB.edges().stream().toArray(Edge[]::new);
        Edge[] gCedges = gC.edges().stream().toArray(Edge[]::new);

        Edge[] gB_intersect_gC_edges = gB.intersection(gC).edges().stream().toArray(Edge[]::new);
        assertGraphsEqual(gA.intersection(gB_intersect_gC_edges), gA.intersection(gBedges).intersection(gCedges));
    }
}
