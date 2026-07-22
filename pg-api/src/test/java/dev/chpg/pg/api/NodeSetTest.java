package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

public class NodeSetTest {

    private static class DummyNode implements Node {
        private final int id;
        private final TagSet tags;
        private final AttributeMap attributes;

        public DummyNode(int id) {
            this.id = id;
            this.tags = new dev.chpg.pg.api.TagSet() {
                private java.util.Set<String> set = new java.util.HashSet<>();
                @Override public boolean add(String tag) { return set.add(tag); }
                @Override public boolean remove(Object tag) { return set.remove(tag); }
                @Override public boolean contains(Object tag) { return set.contains(tag); }
                @Override public void clear() { set.clear(); }
                @Override public int size() { return set.size(); }
                @Override public boolean isEmpty() { return set.isEmpty(); }
                @Override public Iterator<String> iterator() { return set.iterator(); }
                @Override public Object[] toArray() { return set.toArray(); }
                @Override public <T> T[] toArray(T[] a) { return set.toArray(a); }
                @Override public boolean containsAll(Collection<?> c) { return set.containsAll(c); }
                @Override public boolean addAll(Collection<? extends String> c) { return set.addAll(c); }
                @Override public boolean retainAll(Collection<?> c) { return set.retainAll(c); }
                @Override public boolean removeAll(Collection<?> c) { return set.removeAll(c); }
            };
            this.attributes = new dev.chpg.pg.api.AttributeMap() {
                private java.util.Map<String, AttributeValue> map = new java.util.HashMap<>();
                @Override public AttributeValue put(String key, AttributeValue value) { return map.put(key, value); }
                @Override public AttributeValue get(Object key) { return map.get(key); }
                @Override public boolean containsKey(Object key) { return map.containsKey(key); }
                @Override public AttributeValue remove(Object key) { return map.remove(key); }
                @Override public void clear() { map.clear(); }
                @Override public java.util.Set<String> keySet() { return map.keySet(); }
                @Override public int size() { return map.size(); }
                @Override public boolean isEmpty() { return map.isEmpty(); }
                @Override public boolean containsValue(Object value) { return map.containsValue(value); }
                @Override public void putAll(java.util.Map<? extends String, ? extends AttributeValue> m) { map.putAll(m); }
                @Override public java.util.Collection<AttributeValue> values() { return map.values(); }
                @Override public java.util.Set<java.util.Map.Entry<String, AttributeValue>> entrySet() { return map.entrySet(); }

                @Override public AttributeValue put(String key, String value) { return map.put(key, AttributeValue.value(value)); }
                @Override public AttributeValue put(String key, int value) { return map.put(key, AttributeValue.value(value)); }
                @Override public AttributeValue put(String key, long value) { return map.put(key, AttributeValue.value(value)); }
                @Override public AttributeValue put(String key, double value) { return map.put(key, AttributeValue.value(value)); }
                @Override public AttributeValue put(String key, boolean value) { return map.put(key, AttributeValue.value(value)); }
                @Override public AttributeValue put(String key, byte[] value) { return map.put(key, AttributeValue.value(value)); }
            };
        }

        @Override public int id() { return id; }
        @Override public TagSet tags() { return tags; }
        @Override public AttributeMap attributes() { return attributes; }
    }

    @Test
    public void testEmpty() {
        NodeSet empty = NodeSet.empty();
        assertTrue(empty.isEmpty());
        assertTrue(empty instanceof ImmutableEmptyNodeSet);

        NodeSet empty2 = NodeSet.empty();
        assertEquals(empty, empty2);
    }

    @Test
    public void testWithAttribute() {
        DummyNode n1 = new DummyNode(1);
        n1.attributes().put("name", AttributeValue.value("Alice"));
        DummyNode n2 = new DummyNode(2);

        NodeSet set = new GenericImmutableNodeSet(List.of(n1, n2));

        NodeSet filtered = set.withAttribute("name");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(n1));
        assertFalse(filtered.contains(n2));
    }

    @Test
    public void testWithAttributeValues() {
        DummyNode n1 = new DummyNode(1);
        n1.attributes().put("name", AttributeValue.value("Alice"));
        DummyNode n2 = new DummyNode(2);
        n2.attributes().put("name", AttributeValue.value("Bob"));

        NodeSet set = new GenericImmutableNodeSet(List.of(n1, n2));

        NodeSet filtered = set.withAttribute("name", AttributeValue.value("Alice"));
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(n1));

        NodeSet filtered2 = set.withAttribute("name", AttributeValue.value("Charlie"));
        assertTrue(filtered2.isEmpty());

        NodeSet filtered3 = set.withAttribute("age", AttributeValue.value("Alice"));
        assertTrue(filtered3.isEmpty());

        NodeSet filtered4 = set.withAttribute("name", (AttributeValue[]) null);
        assertTrue(filtered4.isEmpty());
    }

    @Test
    public void testWithAnyTag() {
        DummyNode n1 = new DummyNode(1);
        n1.tags().add("Person");
        DummyNode n2 = new DummyNode(2);
        n2.tags().add("Animal");

        NodeSet set = new GenericImmutableNodeSet(List.of(n1, n2));

        NodeSet filtered = set.withAnyTag("Person", "Vehicle");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(n1));

        NodeSet filtered2 = set.withAnyTag((String[]) null);
        assertTrue(filtered2.isEmpty());

        NodeSet filtered3 = set.withAnyTag();
        assertTrue(filtered3.isEmpty());
    }

    @Test
    public void testWithAllTags() {
        DummyNode n1 = new DummyNode(1);
        n1.tags().add("Person");
        n1.tags().add("Employee");

        DummyNode n2 = new DummyNode(2);
        n2.tags().add("Person");

        NodeSet set = new GenericImmutableNodeSet(List.of(n1, n2));

        NodeSet filtered = set.withAllTags("Person", "Employee");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(n1));

        NodeSet filtered2 = set.withAllTags("Person");
        assertEquals(2, filtered2.size());

        NodeSet filtered3 = set.withAllTags((String[]) null);
        assertTrue(filtered3.isEmpty());

        NodeSet filtered4 = set.withAllTags();
        assertTrue(filtered4.isEmpty());
    }

    @Test
    public void testMaterializeDefault() {
        DummyNode n1 = new DummyNode(1);
        NodeSet set = new DeferredNodeSet(new GenericImmutableNodeSet(List.of(n1)), n -> true);

        NodeSet materialized = set.materialize();
        assertTrue(materialized instanceof GenericImmutableNodeSet);
        assertEquals(1, materialized.size());

        NodeSet emptySet = new DeferredNodeSet(new GenericImmutableNodeSet(List.of(n1)), n -> false);
        NodeSet materializedEmpty = emptySet.materialize();
        assertTrue(materializedEmpty instanceof ImmutableEmptyNodeSet);

        // Add direct coverage of the default NodeSet materialize method
        NodeSet directMaterializeSet = new NodeSet() {
            @Override public Iterator<Node> iterator() { return List.<Node>of(n1).iterator(); }
            @Override public int size() { return 1; }
            @Override public boolean isEmpty() { return false; }
            @Override public boolean contains(Object o) { return o == n1; }
            @Override public Object[] toArray() { return new Object[]{n1}; }
            @Override public <T> T[] toArray(T[] a) { return (T[]) new Object[]{n1}; }
            @Override public boolean add(Node node) { return false; }
            @Override public boolean remove(Object o) { return false; }
            @Override public boolean containsAll(Collection<?> c) { return false; }
            @Override public boolean addAll(Collection<? extends Node> c) { return false; }
            @Override public boolean retainAll(Collection<?> c) { return false; }
            @Override public boolean removeAll(Collection<?> c) { return false; }
            @Override public void clear() {}
            @Override public NodeSet toImmutable() { return this; }
            @Override public java.util.Optional<Node> one() { return java.util.Optional.of(n1); }
            @Override public NodeSet intersect(Collection<? extends Node> other) { return this; }
            @Override public NodeSet difference(Collection<? extends Node> other) { return this; }
            @Override public NodeSet union(Collection<? extends Node> other) { return this; }
            @Override public boolean isMaterialized() { return false; }
            @Override public java.util.Set<Integer> ids() { return java.util.Collections.singleton(1); }
            @Override public int[] toIdArray() { return new int[]{1}; }
        };

        NodeSet directlyMaterialized = directMaterializeSet.materialize();
        assertTrue(directlyMaterialized instanceof GenericImmutableNodeSet);
        assertEquals(1, directlyMaterialized.size());

        NodeSet emptyDirectMaterializeSet = new NodeSet() {
            @Override public Iterator<Node> iterator() { return List.<Node>of().iterator(); }
            @Override public int size() { return 0; }
            @Override public boolean isEmpty() { return true; }
            @Override public boolean contains(Object o) { return false; }
            @Override public Object[] toArray() { return new Object[]{}; }
            @Override public <T> T[] toArray(T[] a) { return (T[]) new Object[]{}; }
            @Override public boolean add(Node node) { return false; }
            @Override public boolean remove(Object o) { return false; }
            @Override public boolean containsAll(Collection<?> c) { return false; }
            @Override public boolean addAll(Collection<? extends Node> c) { return false; }
            @Override public boolean retainAll(Collection<?> c) { return false; }
            @Override public boolean removeAll(Collection<?> c) { return false; }
            @Override public void clear() {}
            @Override public NodeSet toImmutable() { return this; }
            @Override public java.util.Optional<Node> one() { return java.util.Optional.empty(); }
            @Override public NodeSet intersect(Collection<? extends Node> other) { return this; }
            @Override public NodeSet difference(Collection<? extends Node> other) { return this; }
            @Override public NodeSet union(Collection<? extends Node> other) { return this; }
            @Override public boolean isMaterialized() { return false; }
            @Override public java.util.Set<Integer> ids() { return java.util.Collections.emptySet(); }
            @Override public int[] toIdArray() { return new int[]{}; }
        };

        NodeSet emptyDirectlyMaterialized = emptyDirectMaterializeSet.materialize();
        assertTrue(emptyDirectlyMaterialized instanceof ImmutableEmptyNodeSet);
        assertEquals(0, emptyDirectlyMaterialized.size());
    }
}
