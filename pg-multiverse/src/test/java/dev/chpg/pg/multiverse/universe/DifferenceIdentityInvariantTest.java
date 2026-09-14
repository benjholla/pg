package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet;

/**
 * Validates invariant identity properties related to set differences on graphs.
 */
public class DifferenceIdentityInvariantTest {
    private Universe universe;

    private Graph gA, gB, gC;


    @BeforeEach
    public void setUp() {
        universe = new Universe();
        EphemeralFactory factory = new EphemeralGraph(universe).factory();

        Node a = factory.createNode();
        a.attributes().put("id", new AttributeValue.IntegerValue(1));
        Node b = factory.createNode();
        b.attributes().put("id", new AttributeValue.IntegerValue(2));
        Node c = factory.createNode();
        c.attributes().put("id", new AttributeValue.IntegerValue(3));
        Node d = factory.createNode();
        d.attributes().put("id", new AttributeValue.IntegerValue(4));

        Edge ab = factory.createEdge(a, b);
        ab.attributes().put("id", new AttributeValue.IntegerValue(12));
        Edge bc = factory.createEdge(b, c);
        bc.attributes().put("id", new AttributeValue.IntegerValue(23));
        Edge cd = factory.createEdge(c, d);
        cd.attributes().put("id", new AttributeValue.IntegerValue(34));

        Graph ephA = factory.createGraph(new EphemeralNodeSet(a, b, c));
        ephA.addEdge(ab);
        ephA.addEdge(bc);

        Graph ephB = factory.createGraph(new EphemeralNodeSet(b, c, d));
        ephB.addEdge(bc);
        ephB.addEdge(cd);

        Graph ephC = factory.createGraph(new EphemeralNodeSet(a, d));

        gA = universe.promote((EphemeralGraph) ephA);
        gB = universe.promote((EphemeralGraph) ephB);
        gC = universe.promote((EphemeralGraph) ephC);
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

    @Test
    public void testDifferenceNodeIdentity() {
        // A \ n == A \ {n}
        Node a = gA.nodes().one().get();
        Graph aMinusNode = gA.difference(a);
        Graph aMinusGraphNode = gA.difference(gA.difference(gA).union(a));

        assertGraphsEqual(aMinusNode, aMinusGraphNode);
    }

    @Test
    public void testDifferenceEdgeIdentity() {
        // API Contract: Graph.difference(Edge) inherently removes the terminal nodes of the edge.
        // Therefore, subtracting a single edge should result in the same graph as subtracting
        // a graph containing that edge (which also contains its terminal nodes due to auto-vivification).
        // A \ e == A \ {e}
        Edge e = gA.edges().one().get();
        Graph aMinusEdge = gA.difference(e);

        Graph gE = gA.difference(gA).union(e);
        Graph aMinusGraphEdge = gA.difference(gE);

        assertGraphsEqual(aMinusEdge, aMinusGraphEdge);
    }

    @Test
    public void testDifferenceEdgesEdgeIdentity() {
        // A \_e e == A \_e {e}
        Edge e = gA.edges().one().get();
        Graph aMinusEdgesEdge = gA.differenceEdges(e);

        Graph gE = gA.difference(gA).union(e);
        Graph aMinusEdgesGraphEdge = gA.differenceEdges(gE);

        assertGraphsEqual(aMinusEdgesEdge, aMinusEdgesGraphEdge);
    }
}
