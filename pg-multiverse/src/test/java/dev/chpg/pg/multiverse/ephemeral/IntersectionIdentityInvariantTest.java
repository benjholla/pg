package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class IntersectionIdentityInvariantTest {

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
    public void testIntersectionCommutativity() {
        Graph aIntB = gA.intersection(gB);
        Graph bIntA = gB.intersection(gA);

        assertGraphsEqual(aIntB, bIntA);
    }

    @Test
    public void testIntersectionAssociativity() {
        Graph aIntBIntC = (gA.intersection(gB)).intersection(gC);
        Graph aIntBIntC2 = gA.intersection(gB.intersection(gC));

        assertGraphsEqual(aIntBIntC, aIntBIntC2);
    }

    @Test
    public void testIntersectionIdempotence() {
        Graph aIntA = gA.intersection(gA);
        assertGraphsEqual(gA, aIntA);
    }

    @Test
    public void testIntersectionNodeIdentity() {
        Node a = gA.nodes().one().get();
        Graph aIntNode = gA.intersection(a);

        Graph gNode = factory.createGraph(a);
        Graph aIntGraphNode = gA.intersection(gNode);

        assertGraphsEqual(aIntNode, aIntGraphNode);
    }

    @Test
    public void testIntersectionEdgeIdentity() {
        Edge e = gA.edges().one().get();
        Graph aIntEdge = gA.intersection(e);

        Graph gE = factory.createGraph();
        gE.addEdge(e);
        Graph aIntGraphEdge = gA.intersection(gE);

        assertGraphsEqual(aIntEdge, aIntGraphEdge);
    }
}
