package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class DistributiveLawsInvariantTest {

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

        gA = factory.createGraph(new EphemeralNodeSet(a, b));
        gA.addEdge(ab);

        gB = factory.createGraph(new EphemeralNodeSet(b, c));
        gB.addEdge(bc);

        gC = factory.createGraph(new EphemeralNodeSet(c, d));
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
