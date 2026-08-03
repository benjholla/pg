package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet;

public class AssociativityLawsInvariantTest {
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

        Graph ephA = factory.createGraph(new EphemeralNodeSet(a, b, c, d));
        ephA.addEdge(ab);
        ephA.addEdge(bc);
        ephA.addEdge(cd);

        Graph promotedA = universe.promote((EphemeralGraph) ephA);

        Graph empty = promotedA.difference(promotedA);

        Node uA = null, uB = null, uC = null, uD = null;
        for (Node n : promotedA.nodes()) {
            if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(1))) uA = n;
            if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(2))) uB = n;
            if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(3))) uC = n;
            if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(4))) uD = n;
        }

        Edge uAB = null, uBC = null, uCD = null;
        for (Edge e : promotedA.edges()) {
            if (e.attributes().get("id").equals(new AttributeValue.IntegerValue(12))) uAB = e;
            if (e.attributes().get("id").equals(new AttributeValue.IntegerValue(23))) uBC = e;
            if (e.attributes().get("id").equals(new AttributeValue.IntegerValue(34))) uCD = e;
        }

        gA = empty.union(uA).union(uB).union(uAB);
        gB = empty.union(uB).union(uC).union(uBC);
        gC = empty.union(uC).union(uD).union(uCD);
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
    }

    @Test
    public void testIntersectionAssociativity() {
        // (A ∩ B) ∩ C == A ∩ (B ∩ C)
        Graph aIntersectB_IntersectC = gA.intersection(gB).intersection(gC);
        Graph aIntersect_BIntersectC = gA.intersection(gB.intersection(gC));
        assertGraphsEqual(aIntersectB_IntersectC, aIntersect_BIntersectC);
    }
}
