package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

public class EdgeSetTest {

    private static class DummyNode implements Node {
        private final int id;
        public DummyNode(int id) { this.id = id; }
        @Override public int id() { return id; }
        @Override public TagSet tags() { return null; }
        @Override public AttributeMap attributes() { return null; }
    }

    private static class DummyEdge implements Edge {
        private final int id;
        private final TagSet tags;
        private final AttributeMap attributes;

        public DummyEdge(int id) {
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
        @Override public Node from() { return new DummyNode(1); }
        @Override public Node to() { return new DummyNode(2); }
    }

    @Test
    public void testEmpty() {
        EdgeSet empty = EdgeSet.empty();
        assertTrue(empty.isEmpty());
        assertTrue(empty instanceof ImmutableEmptyEdgeSet);

        EdgeSet empty2 = EdgeSet.empty();
        assertEquals(empty, empty2);
    }

    @Test
    public void testWithAttribute() {
        DummyEdge e1 = new DummyEdge(1);
        e1.attributes().put("name", AttributeValue.value("Alice"));
        DummyEdge e2 = new DummyEdge(2);

        EdgeSet set = new GenericImmutableEdgeSet(List.of(e1, e2));

        EdgeSet filtered = set.withAttribute("name");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e1));
        assertFalse(filtered.contains(e2));
    }

    @Test
    public void testWithAttributeValues() {
        DummyEdge e1 = new DummyEdge(1);
        e1.attributes().put("name", AttributeValue.value("Alice"));
        DummyEdge e2 = new DummyEdge(2);
        e2.attributes().put("name", AttributeValue.value("Bob"));

        EdgeSet set = new GenericImmutableEdgeSet(List.of(e1, e2));

        EdgeSet filtered = set.withAttribute("name", AttributeValue.value("Alice"));
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e1));

        EdgeSet filtered2 = set.withAttribute("name", AttributeValue.value("Charlie"));
        assertTrue(filtered2.isEmpty());

        EdgeSet filtered3 = set.withAttribute("age", AttributeValue.value("Alice"));
        assertTrue(filtered3.isEmpty());

        EdgeSet filtered4 = set.withAttribute("name", (AttributeValue[]) null);
        assertTrue(filtered4.isEmpty());
    }

    @Test
    public void testWithAnyTag() {
        DummyEdge e1 = new DummyEdge(1);
        e1.tags().add("Person");
        DummyEdge e2 = new DummyEdge(2);
        e2.tags().add("Animal");

        EdgeSet set = new GenericImmutableEdgeSet(List.of(e1, e2));

        EdgeSet filtered = set.withAnyTag("Person", "Vehicle");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e1));

        EdgeSet filtered2 = set.withAnyTag((String[]) null);
        assertTrue(filtered2.isEmpty());

        EdgeSet filtered3 = set.withAnyTag();
        assertTrue(filtered3.isEmpty());
    }

    @Test
    public void testWithAllTags() {
        DummyEdge e1 = new DummyEdge(1);
        e1.tags().add("Person");
        e1.tags().add("Employee");

        DummyEdge e2 = new DummyEdge(2);
        e2.tags().add("Person");

        EdgeSet set = new GenericImmutableEdgeSet(List.of(e1, e2));

        EdgeSet filtered = set.withAllTags("Person", "Employee");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(e1));

        EdgeSet filtered2 = set.withAllTags("Person");
        assertEquals(2, filtered2.size());

        EdgeSet filtered3 = set.withAllTags((String[]) null);
        assertTrue(filtered3.isEmpty());

        EdgeSet filtered4 = set.withAllTags();
        assertTrue(filtered4.isEmpty());
    }

    @Test
    public void testMaterializeDefault() {
        DummyEdge e1 = new DummyEdge(1);
        EdgeSet set = new DeferredEdgeSet(new GenericImmutableEdgeSet(List.of(e1)), e -> true);

        EdgeSet materialized = set.materialize();
        assertTrue(materialized instanceof GenericImmutableEdgeSet);
        assertEquals(1, materialized.size());

        EdgeSet emptySet = new DeferredEdgeSet(new GenericImmutableEdgeSet(List.of(e1)), e -> false);
        EdgeSet materializedEmpty = emptySet.materialize();
        assertTrue(materializedEmpty instanceof ImmutableEmptyEdgeSet);

        // Add direct coverage of the default EdgeSet materialize method
        EdgeSet directMaterializeSet = new EdgeSet() {
            @Override public Iterator<Edge> iterator() { return List.<Edge>of(e1).iterator(); }
            @Override public int size() { return 1; }
            @Override public boolean isEmpty() { return false; }
            @Override public boolean contains(Object o) { return o == e1; }
            @Override public Object[] toArray() { return new Object[]{e1}; }
            @Override public <T> T[] toArray(T[] a) { return (T[]) new Object[]{e1}; }
            @Override public boolean add(Edge node) { return false; }
            @Override public boolean remove(Object o) { return false; }
            @Override public boolean containsAll(Collection<?> c) { return false; }
            @Override public boolean addAll(Collection<? extends Edge> c) { return false; }
            @Override public boolean retainAll(Collection<?> c) { return false; }
            @Override public boolean removeAll(Collection<?> c) { return false; }
            @Override public void clear() {}
            @Override public EdgeSet toImmutable() { return this; }
            @Override public java.util.Optional<Edge> one() { return java.util.Optional.of(e1); }
            @Override public EdgeSet intersect(Collection<? extends Edge> other) { return this; }
            @Override public EdgeSet difference(Collection<? extends Edge> other) { return this; }
            @Override public EdgeSet union(Collection<? extends Edge> other) { return this; }
            @Override public boolean isMaterialized() { return false; }
            @Override public java.util.Set<Integer> ids() { return java.util.Collections.singleton(1); }
            @Override public int[] toIdArray() { return new int[]{1}; }
        };

        EdgeSet directlyMaterialized = directMaterializeSet.materialize();
        assertTrue(directlyMaterialized instanceof GenericImmutableEdgeSet);
        assertEquals(1, directlyMaterialized.size());

        EdgeSet emptyDirectMaterializeSet = new EdgeSet() {
            @Override public Iterator<Edge> iterator() { return List.<Edge>of().iterator(); }
            @Override public int size() { return 0; }
            @Override public boolean isEmpty() { return true; }
            @Override public boolean contains(Object o) { return false; }
            @Override public Object[] toArray() { return new Object[]{}; }
            @Override public <T> T[] toArray(T[] a) { return (T[]) new Object[]{}; }
            @Override public boolean add(Edge node) { return false; }
            @Override public boolean remove(Object o) { return false; }
            @Override public boolean containsAll(Collection<?> c) { return false; }
            @Override public boolean addAll(Collection<? extends Edge> c) { return false; }
            @Override public boolean retainAll(Collection<?> c) { return false; }
            @Override public boolean removeAll(Collection<?> c) { return false; }
            @Override public void clear() {}
            @Override public EdgeSet toImmutable() { return this; }
            @Override public java.util.Optional<Edge> one() { return java.util.Optional.empty(); }
            @Override public EdgeSet intersect(Collection<? extends Edge> other) { return this; }
            @Override public EdgeSet difference(Collection<? extends Edge> other) { return this; }
            @Override public EdgeSet union(Collection<? extends Edge> other) { return this; }
            @Override public boolean isMaterialized() { return false; }
            @Override public java.util.Set<Integer> ids() { return java.util.Collections.emptySet(); }
            @Override public int[] toIdArray() { return new int[]{}; }
        };

        EdgeSet emptyDirectlyMaterialized = emptyDirectMaterializeSet.materialize();
        assertTrue(emptyDirectlyMaterialized instanceof ImmutableEmptyEdgeSet);
        assertEquals(0, emptyDirectlyMaterialized.size());
    }
}
