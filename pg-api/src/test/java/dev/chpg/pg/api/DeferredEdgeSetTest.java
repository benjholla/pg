package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeferredEdgeSetTest {

    private Edge e1, e2, e3;
    private EdgeSet source;
    private DeferredEdgeSet deferredSet;

    @BeforeEach
    public void setUp() {
        Node n1 = new TestNode(1);
        Node n2 = new TestNode(2);

        e1 = new TestEdge(100, n1, n2, "dependsOn", "weight", 1.5);
        e2 = new TestEdge(101, n2, n1, "calls", "count", 10);
        e3 = new TestEdge(102, n1, n1, "selfLoop", "type", "internal");

        source = new GenericImmutableEdgeSet(List.of(e1, e2, e3));
        deferredSet = new DeferredEdgeSet(source, edge -> true);
    }

    @Test
    public void testBasicProperties() {
        assertFalse(deferredSet.isSizeKnown());
        assertFalse(deferredSet.isMaterialized());
        assertEquals(3, deferredSet.size());
        assertFalse(deferredSet.isEmpty());
        assertTrue(deferredSet.contains(e1));
        assertFalse(deferredSet.contains(new TestEdge(999, new TestNode(1), new TestNode(2))));
        assertEquals(3, deferredSet.ids().size());
        assertEquals(3, deferredSet.toIdArray().length);
    }

    @Test
    public void testIterator() {
        Iterator<Edge> it = deferredSet.iterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(3, count);

        org.junit.jupiter.api.Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    public void testOne() {
        assertTrue(deferredSet.one().isPresent());

        DeferredEdgeSet empty = new DeferredEdgeSet(source, e -> false);
        assertFalse(empty.one().isPresent());
    }

    @Test
    public void testWithAttribute() {
        EdgeSet filtered = deferredSet.withAttribute("type");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e3));

        EdgeSet filteredVal = deferredSet.withAttribute("count", AttributeValue.value(10));
        assertEquals(1, filteredVal.size());
        assertTrue(filteredVal.contains(e2));

        EdgeSet filteredEmptyVal = deferredSet.withAttribute("count");
        assertEquals(1, filteredEmptyVal.size());
    }

    @Test
    public void testWithAnyTag() {
        EdgeSet filtered = deferredSet.withAnyTag("dependsOn", "Unknown");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e1));
    }

    @Test
    public void testWithAllTags() {
        TestEdge e4 = new TestEdge(104, new TestNode(1), new TestNode(2), "dependsOn");
        e4.tags.add("critical");

        EdgeSet newSource = new GenericImmutableEdgeSet(List.of(e1, e4));
        DeferredEdgeSet newDef = new DeferredEdgeSet(newSource, e -> true);

        EdgeSet filteredAll = newDef.withAllTags("dependsOn", "critical");
        assertEquals(1, filteredAll.size());
        assertTrue(filteredAll.contains(e4));
    }

    @Test
    public void testMaterializeAndImmutable() {
        EdgeSet materialized = deferredSet.materialize();
        assertTrue(materialized.isMaterialized());
        assertEquals(3, materialized.size());

        EdgeSet immutable = deferredSet.toImmutable();
        assertTrue(immutable.isMaterialized());
        assertEquals(3, immutable.size());

        DeferredEdgeSet emptyDef = new DeferredEdgeSet(source, e -> false);
        assertTrue(emptyDef.materialize().isEmpty());
    }

    @Test
    public void testSetOperations() {
        EdgeSet other = new GenericImmutableEdgeSet(List.of(e1));

        EdgeSet intersect = deferredSet.intersect(other);
        assertEquals(1, intersect.size());
        assertTrue(intersect.contains(e1));

        EdgeSet difference = deferredSet.difference(other);
        assertEquals(2, difference.size());
        assertTrue(difference.contains(e2));
        assertTrue(difference.contains(e3));

        EdgeSet union = deferredSet.withAttribute("type").union(new GenericImmutableEdgeSet(List.of(e1)));
        assertEquals(2, union.size());
        assertTrue(union.contains(e3));
        assertTrue(union.contains(e1));
    }

    private static class TestNode implements Node {
        private final int id;
        public TestNode(int id) { this.id = id; }
        @Override public int id() { return id; }
        @Override public TagSet tags() { return null; }
        @Override public AttributeMap attributes() { return null; }
    }

    private static class TestEdge implements Edge {
        private final int id;
        private final Node from;
        private final Node to;
        public final TagSet tags = new TagSetTestImpl();
        public final AttributeMap attributes = new AttributeMapTestImpl();

        public TestEdge(int id, Node from, Node to, String tag, String attrKey, Object attrVal) {
            this.id = id;
            this.from = from;
            this.to = to;
            if (tag != null) tags.add(tag);
            if (attrKey != null && attrVal != null) {
                if (attrVal instanceof String) attributes.put(attrKey, (String) attrVal);
                if (attrVal instanceof Integer) attributes.put(attrKey, (Integer) attrVal);
                if (attrVal instanceof Double) attributes.put(attrKey, (Double) attrVal);
            }
        }

        public TestEdge(int id, Node from, Node to, String tag) {
            this(id, from, to, tag, null, null);
        }

        public TestEdge(int id, Node from, Node to) {
            this(id, from, to, null, null, null);
        }

        @Override public int id() { return id; }
        @Override public Node from() { return from; }
        @Override public Node to() { return to; }
        @Override public TagSet tags() { return tags; }
        @Override public AttributeMap attributes() { return attributes; }

        @Override
        public int hashCode() { return id; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            return id == ((TestEdge) obj).id;
        }
    }

    private static class TagSetTestImpl extends java.util.HashSet<String> implements TagSet {}
    private static class AttributeMapTestImpl extends java.util.HashMap<String, AttributeValue> implements AttributeMap {
        @Override public AttributeValue put(String key, String value) { return put(key, AttributeValue.value(value)); }
        @Override public AttributeValue put(String key, int value) { return put(key, AttributeValue.value(value)); }
        @Override public AttributeValue put(String key, long value) { return put(key, AttributeValue.value(value)); }
        @Override public AttributeValue put(String key, double value) { return put(key, AttributeValue.value(value)); }
        @Override public AttributeValue put(String key, boolean value) { return put(key, AttributeValue.value(value)); }
        @Override public AttributeValue put(String key, byte[] value) { return put(key, AttributeValue.value(value)); }
    }
}