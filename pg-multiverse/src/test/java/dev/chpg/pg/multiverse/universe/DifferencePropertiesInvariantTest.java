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
 * Validates properties related to set differences on graphs.
 */
public class DifferencePropertiesInvariantTest {
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

        Graph promotedA = universe.promote((EphemeralGraph) ephA);

        gA = promotedA;

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
    public void testDifferenceFromEmpty() {
        // ∅ \ A = ∅
        Graph empty = gA.difference(gA);
        Graph diff = empty.difference(gA);
        assertTrue(diff.nodes().isEmpty());
    }

    @Test
    public void testDifferenceOfEmpty() {
        // A \ ∅ = A
        Graph empty = gA.difference(gA);
        Graph diff = gA.difference(empty);
        assertGraphsEqual(gA, diff);
    }

    @Test
    public void testDifferenceSelf() {
        // A \ A = ∅
        Graph diff = gA.difference(gA);
        assertTrue(diff.nodes().isEmpty());
    }

    @Test
    public void testDifferenceIntersection() {
        // A \ (A \ B) ⊇ A ∩ B
        Graph aMinusB = gA.difference(gB);
        Graph aMinusAMinusB = gA.difference(aMinusB);
        Graph aIntB = gA.intersection(gB);

        assertTrue(aMinusAMinusB.nodes().containsAll(aIntB.nodes()), "A \\ (A \\ B) should contain all nodes of A ∩ B");
        assertTrue(aMinusAMinusB.edges().containsAll(aIntB.edges()), "A \\ (A \\ B) should contain all edges of A ∩ B");
    }
}
