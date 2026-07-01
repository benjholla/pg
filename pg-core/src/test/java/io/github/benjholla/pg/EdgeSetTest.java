package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Optional;

public class EdgeSetTest {

    private Node n1, n2, n3;
    private Edge e1, e2, e3;
    private EdgeSet edgeSet;

    @BeforeEach
    public void setUp() {
        n1 = new Node();
        n2 = new Node();
        n3 = new Node();

        e1 = new Edge(n1, n2);
        e1.putAttr("type", "A");
        e1.putAttr("val", 1);

        e2 = new Edge(n2, n3);
        e2.putAttr("type", "B");
        e2.putAttr("val", 2);

        e3 = new Edge(n3, n1);
        e3.putAttr("type", "A");
        e3.putAttr("val", 3);

        edgeSet = new EdgeSet(e1, e2, e3);
    }

    @Test
    public void testConstructors() {
        EdgeSet empty = new EdgeSet();
        assertTrue(empty.isEmpty());

        EdgeSet single = new EdgeSet(e1);
        assertEquals(1, single.size());
        assertTrue(single.contains(e1));

        EdgeSet multi = new EdgeSet(e1, e2);
        assertEquals(2, multi.size());
        assertTrue(multi.contains(e1));
        assertTrue(multi.contains(e2));

        EdgeSet coll = new EdgeSet(Arrays.asList(e2, e3));
        assertEquals(2, coll.size());
        assertTrue(coll.contains(e2));
        assertTrue(coll.contains(e3));
    }

    @Test
    public void testOne() {
        Optional<Edge> optEdge = edgeSet.one();
        assertTrue(optEdge.isPresent());
        assertTrue(edgeSet.contains(optEdge.get()));

        EdgeSet empty = new EdgeSet();
        assertFalse(empty.one().isPresent());
    }

    @Test
    public void testFilterByAttributePresent() {
        e1.putAttr("unique", true);
        EdgeSet filtered = edgeSet.filter("unique");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e1));

        EdgeSet filteredType = edgeSet.filter("type");
        assertEquals(3, filteredType.size());
    }

    @Test
    public void testFilterByAttributeAndValues() {
        EdgeSet filteredTypeA = edgeSet.filter("type", "A");
        assertEquals(2, filteredTypeA.size());
        assertTrue(filteredTypeA.contains(e1));
        assertTrue(filteredTypeA.contains(e3));

        EdgeSet filteredVal = edgeSet.filter("val", 1, 3);
        assertEquals(2, filteredVal.size());
        assertTrue(filteredVal.contains(e1));
        assertTrue(filteredVal.contains(e3));

        EdgeSet filteredNone = edgeSet.filter("type", "C");
        assertTrue(filteredNone.isEmpty());

        EdgeSet nullAttr = edgeSet.filter(null, "A");
        assertTrue(nullAttr.isEmpty());

        EdgeSet nullVals = edgeSet.filter("type", (Object[]) null);
        assertTrue(nullVals.isEmpty());

        EdgeSet nullAttrOnly = edgeSet.filter((String) null);
        assertTrue(nullAttrOnly.isEmpty());

        EdgeSet nullAttrAndNullVals = edgeSet.filter(null, (Object[]) null);
        assertTrue(nullAttrAndNullVals.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        EdgeSet es1 = new EdgeSet(e1, e2);
        EdgeSet es2 = new EdgeSet(e2, e1);
        assertEquals(es1, es2);
        assertEquals(es1.hashCode(), es2.hashCode());

        EdgeSet es3 = new EdgeSet(e1, e3);
        assertNotEquals(es1, es3);
    }

    @Test
    public void testToString() {
        EdgeSet es = new EdgeSet(e1);
        String str = es.toString();
        assertTrue(str.startsWith("EdgeSet [edges="));
        assertTrue(str.contains(e1.toString()));
        assertTrue(str.endsWith("]"));
    }
}
