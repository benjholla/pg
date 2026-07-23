package dev.chpg.pg.multiverse.ephemeral;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.AttributeValue;

/**
 * Attributes map arbitrary keys to object values.
 */
public final class EphemeralAttributeMap implements AttributeMap {

    /**
     * undocumented.
     */
    private final Map<String, AttributeValue> delegate;

    /**
     * undocumented.
     */
    public EphemeralAttributeMap() {
        this.delegate = new HashMap<>();
    }

    /**
     * undocumented.
     */
    public EphemeralAttributeMap(Map<? extends String, ? extends AttributeValue> m) {
        this.delegate = new HashMap<>();
        this.putAll(m);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public AttributeValue get(Object key) {
        return delegate.get(key);
    }

    @Override
    public AttributeValue remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<AttributeValue> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<String, AttributeValue>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public AttributeValue put(String key, AttributeValue value) {
        Objects.requireNonNull(key, "Attribute key cannot be null");
        Objects.requireNonNull(value, "Attribute value cannot be null");
        return delegate.put(key, value);
    }

    @Override
    public AttributeValue put(String key, String value) {
        return this.put(key, AttributeValue.value(value));
    }

    @Override
    public AttributeValue put(String key, int value) {
        return this.put(key, AttributeValue.value(value));
    }

    @Override
    public AttributeValue put(String key, long value) {
        return this.put(key, AttributeValue.value(value));
    }

    @Override
    public AttributeValue put(String key, double value) {
        return this.put(key, AttributeValue.value(value));
    }

    @Override
    public AttributeValue put(String key, boolean value) {
        return this.put(key, AttributeValue.value(value));
    }

    @Override
    public AttributeValue put(String key, byte[] value) {
        return this.put(key, AttributeValue.value(value));
    }

    @Override
    public void putAll(Map<? extends String, ? extends AttributeValue> m) {
        Objects.requireNonNull(m, "Attribute map cannot be null");
        for (Map.Entry<? extends String, ? extends AttributeValue> entry : m.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "Attribute key cannot be null");
            Objects.requireNonNull(entry.getValue(), "Attribute value cannot be null");
        }
        delegate.putAll(m);
    }

    @Override
    public AttributeValue putIfAbsent(String key, AttributeValue value) {
        Objects.requireNonNull(key, "Attribute key cannot be null");
        Objects.requireNonNull(value, "Attribute value cannot be null");
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
