package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class UnionIdentityInvariantTest {

    private static final Universe universe = new Universe();
    private static final EphemeralFactory factory = new EphemeralGraph(universe).factory();
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

        gA = factory.createGraph(new EphemeralNodeSet(a, b, c));
        gA.addEdge(ab);
        gA.addEdge(bc);

        gB = factory.createGraph(new EphemeralNodeSet(b, c, d));
        gB.addEdge(bc);
        gB.addEdge(cd);

        gC = factory.createGraph(new EphemeralNodeSet(a, d));
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
        Graph aUnionB = gA.union(gB);
        Graph bUnionA = gB.union(gA);

        assertGraphsEqual(aUnionB, bUnionA);
    }

    @Test
    public void testUnionAssociativity() {
        Graph aUnionBUnionC = (gA.union(gB)).union(gC);
        Graph aUnionBUnionC2 = gA.union(gB.union(gC));

        assertGraphsEqual(aUnionBUnionC, aUnionBUnionC2);
    }

    @Test
    public void testUnionIdempotence() {
        Graph aUnionA = gA.union(gA);
        assertGraphsEqual(gA, aUnionA);
    }

    @Test
    public void testUnionNodeIdentity() {
        Node n = factory.createNode();
        Graph aUnionNode = gA.union(n);

        Graph gNode = factory.createGraph(n);
        Graph aUnionGraphNode = gA.union(gNode);

        assertGraphsEqual(aUnionNode, aUnionGraphNode);
    }

    @Test
    public void testUnionEdgeIdentity() {
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        Graph aUnionEdge = gA.union(e);

        Graph gE = factory.createGraph();
        gE.addEdge(e);
        Graph aUnionGraphEdge = gA.union(gE);

        assertGraphsEqual(aUnionEdge, aUnionGraphEdge);
    }
}
