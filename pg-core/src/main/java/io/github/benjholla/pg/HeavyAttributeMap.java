package io.github.benjholla.pg;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Attributes map arbitrary keys to object values.
 */
public class HeavyAttributeMap extends HashMap<String, AttributeValue> implements AttributeMap {

    private static final long serialVersionUID = 1L;

    public HeavyAttributeMap() {
        super();
    }

    public HeavyAttributeMap(Map<? extends String, ? extends AttributeValue> m) {
        super();
        this.putAll(m);
    }

    @Override
    public AttributeValue put(String key, AttributeValue value) {
        Objects.requireNonNull(key, "Attribute key cannot be null");
        Objects.requireNonNull(value, "Attribute value cannot be null");
        return super.put(key, value);
    }

    public AttributeValue put(String key, String value) {
        return this.put(key, new AttributeValue.StringVal(value));
    }

    public AttributeValue put(String key, int value) {
        return this.put(key, new AttributeValue.IntVal(value));
    }

    public AttributeValue put(String key, long value) {
        return this.put(key, new AttributeValue.LongVal(value));
    }

    public AttributeValue put(String key, double value) {
        return this.put(key, new AttributeValue.DoubleVal(value));
    }

    public AttributeValue put(String key, boolean value) {
        return this.put(key, new AttributeValue.BooleanVal(value));
    }

    public AttributeValue put(String key, byte[] value) {
        return this.put(key, new AttributeValue.ByteArrayVal(value));
    }

    @Override
    public void putAll(Map<? extends String, ? extends AttributeValue> m) {
        Objects.requireNonNull(m, "Attribute map cannot be null");
        for (Map.Entry<? extends String, ? extends AttributeValue> entry : m.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "Attribute key cannot be null");
            Objects.requireNonNull(entry.getValue(), "Attribute value cannot be null");
        }
        super.putAll(m);
    }

    @Override
    public AttributeValue putIfAbsent(String key, AttributeValue value) {
        Objects.requireNonNull(key, "Attribute key cannot be null");
        Objects.requireNonNull(value, "Attribute value cannot be null");
        return super.putIfAbsent(key, value);
    }

}
