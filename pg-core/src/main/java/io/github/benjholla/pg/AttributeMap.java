package io.github.benjholla.pg;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Attributes map arbitrary keys to object values.
 */
public class AttributeMap extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public AttributeMap() {
        super();
    }

    public AttributeMap(Map<? extends String, ? extends Object> m) {
        super();
        this.putAll(m);
    }

    @Override
    public Object put(String key, Object value) {
        Objects.requireNonNull(key, "Attribute key cannot be null");
        Objects.requireNonNull(value, "Attribute value cannot be null");
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        Objects.requireNonNull(m, "Attribute map cannot be null");
        for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "Attribute key cannot be null");
            Objects.requireNonNull(entry.getValue(), "Attribute value cannot be null");
        }
        super.putAll(m);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        Objects.requireNonNull(key, "Attribute key cannot be null");
        Objects.requireNonNull(value, "Attribute value cannot be null");
        return super.putIfAbsent(key, value);
    }

}
