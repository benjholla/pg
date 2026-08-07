package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeferredNodeSetTest {

    private Node n1, n2, n3;
    private NodeSet source;
    private DeferredNodeSet deferredSet;

    @BeforeEach
    public void setUp() {
        n1 = new TestNode(1, "Person", "name", "Alice");
        n2 = new TestNode(2, "Person", "age", 30);
        n3 = new TestNode(3, "Animal", "type", "Dog");

        source = new GenericImmutableNodeSet(List.of(n1, n2, n3));
        deferredSet = new DeferredNodeSet(source, node -> true);
    }

    @Test
    public void testBasicProperties() {
        assertFalse(deferredSet.isSizeKnown());
        assertFalse(deferredSet.isMaterialized());
        assertEquals(3, deferredSet.size());
        assertFalse(deferredSet.isEmpty());
        assertTrue(deferredSet.contains(n1));
        assertFalse(deferredSet.contains(new TestNode(99)));
        assertEquals(3, deferredSet.ids().size());
        assertEquals(3, deferredSet.toIdArray().length);
    }

    @Test
    public void testIterator() {
        Iterator<Node> it = deferredSet.iterator();
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

        DeferredNodeSet empty = new DeferredNodeSet(source, n -> false);
        assertFalse(empty.one().isPresent());
    }

    @Test
    public void testWithAttribute() {
        NodeSet filtered = deferredSet.withAttribute("name");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(n1));

        NodeSet filteredVal = deferredSet.withAttribute("age", AttributeValue.value(30));
        assertEquals(1, filteredVal.size());
        assertTrue(filteredVal.contains(n2));

        NodeSet filteredEmptyVal = deferredSet.withAttribute("age");
        assertEquals(1, filteredEmptyVal.size());
    }

    @Test
    public void testWithAnyTag() {
        NodeSet filtered = deferredSet.withAnyTag("Person", "Unknown");
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(n1));
        assertTrue(filtered.contains(n2));
        assertFalse(filtered.contains(n3));
    }

    @Test
    public void testWithAllTags() {
        NodeSet filtered = deferredSet.withAllTags("Person");
        assertEquals(2, filtered.size());

        TestNode n4 = new TestNode(4, "Person");
        n4.tags.add("Employee");

        NodeSet newSource = new GenericImmutableNodeSet(List.of(n1, n4));
        DeferredNodeSet newDef = new DeferredNodeSet(newSource, n -> true);

        NodeSet filteredAll = newDef.withAllTags("Person", "Employee");
        assertEquals(1, filteredAll.size());
        assertTrue(filteredAll.contains(n4));
    }

    @Test
    public void testMaterializeAndImmutable() {
        NodeSet materialized = deferredSet.materialize();
        assertTrue(materialized.isMaterialized());
        assertEquals(3, materialized.size());

        NodeSet immutable = deferredSet.toImmutable();
        assertTrue(immutable.isMaterialized());
        assertEquals(3, immutable.size());

        DeferredNodeSet emptyDef = new DeferredNodeSet(source, n -> false);
        assertTrue(emptyDef.materialize().isEmpty());
    }

    @Test
    public void testSetOperations() {
        NodeSet other = new GenericImmutableNodeSet(List.of(n1));

        NodeSet intersect = deferredSet.intersect(other);
        assertEquals(1, intersect.size());
        assertTrue(intersect.contains(n1));

        NodeSet difference = deferredSet.difference(other);
        assertEquals(2, difference.size());
        assertTrue(difference.contains(n2));
        assertTrue(difference.contains(n3));

        NodeSet union = deferredSet.withAttribute("name").union(new GenericImmutableNodeSet(List.of(n3)));
        assertEquals(2, union.size());
        assertTrue(union.contains(n1));
        assertTrue(union.contains(n3));
    }

    private static class TestNode implements Node {
        private final int id;
        public final TagSet tags = new TagSetTestImpl();
        public final AttributeMap attributes = new AttributeMapTestImpl();

        public TestNode(int id, String tag, String attrKey, Object attrVal) {
            this.id = id;
            if (tag != null) { tags.add(tag); }
            if (attrKey != null && attrVal != null) {
                if (attrVal instanceof String) { attributes.put(attrKey, (String) attrVal); }
                if (attrVal instanceof Integer) { attributes.put(attrKey, (Integer) attrVal); }
            }
        }

        public TestNode(int id, String tag) {
            this(id, tag, null, null);
        }

        public TestNode(int id) {
            this(id, null, null, null);
        }

        @Override
        public int id() { return id; }

        @Override
        public TagSet tags() { return tags; }

        @Override
        public AttributeMap attributes() { return attributes; }

        @Override
        public int hashCode() { return id; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) { return true; }
            if (obj == null || getClass() != obj.getClass()) { return false; }
            return id == ((TestNode) obj).id;
        }
    }

    private static class TagSetTestImpl extends java.util.HashSet<String> implements TagSet {}
    private static class AttributeMapTestImpl extends java.util.HashMap<String, AttributeValue> implements AttributeMap {
        @Override
        public AttributeValue put(String key, String value) { return put(key, AttributeValue.value(value)); }
        @Override
        public AttributeValue put(String key, int value) { return put(key, AttributeValue.value(value)); }
        @Override
        public AttributeValue put(String key, long value) { return put(key, AttributeValue.value(value)); }
        @Override
        public AttributeValue put(String key, double value) { return put(key, AttributeValue.value(value)); }
        @Override
        public AttributeValue put(String key, boolean value) { return put(key, AttributeValue.value(value)); }
        @Override
        public AttributeValue put(String key, byte[] value) { return put(key, AttributeValue.value(value)); }
    }
}
