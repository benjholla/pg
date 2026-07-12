package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;

public class EdgeSetTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Node n1, n2, n3;
    private Edge e1, e2, e3;
    private EdgeSet edgeSet;

    @BeforeEach
    public void setUp() {
        n1 = factory.createNode();
        n2 = factory.createNode();
        n3 = factory.createNode();

        e1 = factory.createEdge(n1, n2);
        e1.attributes().put("type", "A");
        e1.attributes().put("val", 1);

        e2 = factory.createEdge(n2, n3);
        e2.attributes().put("type", "B");
        e2.attributes().put("val", 2);

        e3 = factory.createEdge(n3, n1);
        e3.attributes().put("type", "A");
        e3.attributes().put("val", 3);

        edgeSet = new EphemeralEdgeSet(e1, e2, e3);
    }

    @Test
    public void testConstructors() {
        EdgeSet empty = new EphemeralEdgeSet();
        assertTrue(empty.isEmpty());

        EdgeSet single = new EphemeralEdgeSet(e1);
        assertEquals(1, single.size());
        assertTrue(single.contains(e1));

        EdgeSet multi = new EphemeralEdgeSet(e1, e2);
        assertEquals(2, multi.size());
        assertTrue(multi.contains(e1));
        assertTrue(multi.contains(e2));

        EdgeSet coll = new EphemeralEdgeSet(Arrays.asList(e2, e3));
        assertEquals(2, coll.size());
        assertTrue(coll.contains(e2));
        assertTrue(coll.contains(e3));
    }

    @Test
    public void testOne() {
        Optional<Edge> optEdge = edgeSet.one();
        assertTrue(optEdge.isPresent());
        assertTrue(edgeSet.contains(optEdge.get()));

        EdgeSet empty = new EphemeralEdgeSet();
        assertFalse(empty.one().isPresent());
    }

    @Test
    public void testFilterByAttributePresent() {
        e1.attributes().put("unique", true);
        EdgeSet filtered = edgeSet.filter("unique");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e1));

        EdgeSet filteredType = edgeSet.filter("type");
        assertEquals(3, filteredType.size());
    }

    @Test
    public void testFilterByAttributeAndValues() {
        EdgeSet filteredTypeA = edgeSet.filter("type", AttributeValue.value("A"));
        assertEquals(2, filteredTypeA.size());
        assertTrue(filteredTypeA.contains(e1));
        assertTrue(filteredTypeA.contains(e3));

        EdgeSet filteredVal = edgeSet.filter("val", AttributeValue.value(1), AttributeValue.value(3));
        assertEquals(2, filteredVal.size());
        assertTrue(filteredVal.contains(e1));
        assertTrue(filteredVal.contains(e3));

        EdgeSet filteredNone = edgeSet.filter("type", AttributeValue.value("C"));
        assertTrue(filteredNone.isEmpty());

        EdgeSet nullAttr = edgeSet.filter(null, AttributeValue.value("A"));
        assertTrue(nullAttr.isEmpty());

        EdgeSet nullVals = edgeSet.filter("type", (AttributeValue[]) null);
        assertTrue(nullVals.isEmpty());

        EdgeSet nullAttrOnly = edgeSet.filter((String) null);
        assertTrue(nullAttrOnly.isEmpty());

        EdgeSet nullAttrAndNullVals = edgeSet.filter(null, (AttributeValue[]) null);
        assertTrue(nullAttrAndNullVals.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        EdgeSet es1 = new EphemeralEdgeSet(e1, e2);
        EdgeSet es2 = new EphemeralEdgeSet(e2, e1);
        assertEquals(es1, es2);
        assertEquals(es1.hashCode(), es2.hashCode());

        EdgeSet es3 = new EphemeralEdgeSet(e1, e3);
        assertNotEquals(es1, es3);
    }

    @Test
    public void testToString() {
        EdgeSet es = new EphemeralEdgeSet(e1);
        String str = es.toString();
        assertTrue(str.startsWith("EphemeralEdgeSet [edges="));
        assertTrue(str.contains(e1.toString()));
        assertTrue(str.endsWith("]"));
    }
}
