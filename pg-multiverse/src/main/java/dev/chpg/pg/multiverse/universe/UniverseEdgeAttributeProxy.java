package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.AttributeValue;

import java.util.AbstractMap;
import java.util.Objects;
import java.util.Set;

/**
 * A zero-allocation, pass-through viewport into the Universe's columnar edge attribute storage.
 */
public final class UniverseEdgeAttributeProxy extends AbstractMap<String, AttributeValue> implements AttributeMap, UniverseView {

    private final Universe universe;
    private final int edgeId;

    UniverseEdgeAttributeProxy(Universe universe, int edgeId) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.edgeId = edgeId;
    }

    @Override public Universe universe() { return this.universe; }
    @Override public int size() { return this.universe.edgeAttributeCount(this.edgeId); }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        return this.universe.hasEdgeAttribute(this.edgeId, (String) key);
    }

    @Override
    public AttributeValue get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return this.universe.getEdgeAttribute(this.edgeId, (String) key);
    }

    @Override
    public AttributeValue put(String key, AttributeValue value) {
        Objects.requireNonNull(key, "Attribute key cannot be null");
        Objects.requireNonNull(value, "Attribute value cannot be null");
        return this.universe.setEdgeAttribute(this.edgeId, key, value);
    }

    // --- Interface Primitive Overloads ---
    @Override public AttributeValue put(String key, String value) { return put(key, AttributeValue.value(value)); }
    @Override public AttributeValue put(String key, int value) { return put(key, AttributeValue.value(value)); }
    @Override public AttributeValue put(String key, long value) { return put(key, AttributeValue.value(value)); }
    @Override public AttributeValue put(String key, double value) { return put(key, AttributeValue.value(value)); }
    @Override public AttributeValue put(String key, boolean value) { return put(key, AttributeValue.value(value)); }
    @Override public AttributeValue put(String key, byte[] value) { return put(key, AttributeValue.value(value)); }

    @Override
    public AttributeValue remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return this.universe.removeEdgeAttribute(this.edgeId, (String) key);
    }


    @Override public Set<Entry<String, AttributeValue>> entrySet() { return this.universe.edgeAttributeEntrySet(this.edgeId); }
    @Override public void clear() { this.universe.clearEdgeAttributes(this.edgeId); }
}
