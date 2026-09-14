package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.multiverse.universe.Universe;
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
 * Validates properties related to differenceEdges operations on graphs.
 */
public class DifferenceEdgesPropertiesInvariantTest {
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

        Edge ab = factory.createEdge(a, b);
        ab.attributes().put("id", new AttributeValue.IntegerValue(12));
        Edge bc = factory.createEdge(b, c);
        bc.attributes().put("id", new AttributeValue.IntegerValue(23));

        Graph ephA = factory.createGraph(new EphemeralNodeSet(a, b, c));
        ephA.addEdge(ab);
        ephA.addEdge(bc);

        gA = universe.promote((EphemeralGraph) ephA);

        Graph empty = gA.difference(gA);

        Node uA = null, uB = null, uC = null;
        for (Node n : gA.nodes()) {
            if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(1))) {
                uA = n;
            }
            if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(2))) {
                uB = n;
            }
            if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(3))) {
                uC = n;
            }
        }

        Edge uAB = null, uBC = null;
        for (Edge e : gA.edges()) {
            if (e.attributes().get("id").equals(new AttributeValue.IntegerValue(12))) {
                uAB = e;
            }
            if (e.attributes().get("id").equals(new AttributeValue.IntegerValue(23))) {
                uBC = e;
            }
        }

        gB = empty.union(uA).union(uB).union(uAB);
        gC = empty.union(uB).union(uC).union(uBC);
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
    public void testDifferenceEdgesFromEmpty() {
        // ∅ \_e A = ∅
        Graph empty = gA.difference(gA);
        Graph diff = empty.differenceEdges(gA);
        assertTrue(diff.nodes().isEmpty());
    }

    @Test
    public void testDifferenceEdgesOfEmpty() {
        // A \_e ∅ = A
        Graph empty = gA.difference(gA);
        Graph diff = gA.differenceEdges(empty);
        assertGraphsEqual(gA, diff);
    }

    @Test
    public void testDifferenceEdgesSelf() {
        // A \_e A = Nodes(A)  (All edges removed, all nodes remain)
        Graph diff = gA.differenceEdges(gA);
        assertEquals(gA.nodes().size(), diff.nodes().size(), "differenceEdges should not remove nodes");
        assertTrue(diff.nodes().containsAll(gA.nodes()), "differenceEdges should retain all nodes");
        assertEquals(0, diff.edges().size(), "differenceEdges self should remove all edges");
    }

    @Test
    public void testDifferenceEdgesDisjoint() {
        // A \_e B = A (if A and B have no edges in common)


        EphemeralFactory factory = new EphemeralGraph(universe).factory();
        Node d = factory.createNode();
        d.attributes().put("id", new AttributeValue.IntegerValue(4));
        Node e = factory.createNode();
        e.attributes().put("id", new AttributeValue.IntegerValue(5));
        Edge de = factory.createEdge(d, e);
        de.attributes().put("id", new AttributeValue.IntegerValue(45));

        Graph gDisjointEph = factory.createGraph(new EphemeralNodeSet(d, e));
        gDisjointEph.addEdge(de);

        Graph promotedDisjoint = universe.promote((EphemeralGraph) gDisjointEph);

        Node uD = null, uE = null;
        for (Node n : promotedDisjoint.nodes()) {
            if (new AttributeValue.IntegerValue(4).equals(n.attributes().get("id"))) {
                uD = n;
            }
            if (new AttributeValue.IntegerValue(5).equals(n.attributes().get("id"))) {
                uE = n;
            }
        }
        Edge uDE = null;
        for (Edge edge : promotedDisjoint.edges()) {
            if (new AttributeValue.IntegerValue(45).equals(edge.attributes().get("id"))) {
                uDE = edge;
            }
        }

        Graph gDisjoint = gA.difference(gA).union(uD).union(uE).union(uDE);



        Graph diff = gA.differenceEdges(gDisjoint);
        assertGraphsEqual(gA, diff);
    }

    @Test
    public void testDifferenceEdgesSubset() {
        // A \_e B should strictly remove the edges present in B
        Graph diff = gA.differenceEdges(gB);
        assertEquals(gA.nodes().size(), diff.nodes().size(), "differenceEdges should not remove nodes");



        assertEquals(1, diff.edges().size(), "One edge should remain");
        boolean containsBc = false;
        for (Edge e : diff.edges()) {
            if (e.attributes().get("id").equals(new AttributeValue.IntegerValue(23))) {
                containsBc = true;
                break;
            }
        }
        assertTrue(containsBc, "The remaining edge should be the one not in B (bc)");
    }
}
