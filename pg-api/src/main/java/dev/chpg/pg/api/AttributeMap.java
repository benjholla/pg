package dev.chpg.pg.api;

import java.util.Map;

/**
 * A specialized map for managing key-value properties on a {@link GraphElement}.
 * <p>
 * <b>What it represents:</b> A property dictionary mapping string keys to strongly-typed {@link AttributeValue}s.
 * <p>
 * <b>Why it exists:</b> To allow graph elements to hold arbitrary data payloads while enforcing a restricted, memory-safe type system that easily serializes to JSON or binary formats.
 * <p>
 * <b>When to use it:</b> Use {@code AttributeMap} when you need to store metadata, weights, coordinates, or domain-specific properties directly on nodes and edges.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Adding properties via overloaded convenience methods (e.g., {@code node.attributes().put("weight", 1.5)}).</li>
 * <li>Retrieving properties (e.g., {@code AttributeValue val = node.attributes().get("name")}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> The map strictly enforces null-safety. Null keys or values are not permitted. Values must conform to the permitted types defined by the sealed {@link AttributeValue} interface.
 * <p>
 * <b>Thread safety:</b> Thread safety guarantees depend on the backing graph implementation. Assume it is not safe for concurrent mutation unless explicitly documented by the backend (e.g., {@code EphemeralGraph}).
 * <p>
 * <b>Performance characteristics:</b> Standard implementations (like Ephemeral) use standard hash maps (O(1) access). The overloaded primitives methods auto-box values into {@link AttributeValue} records to satisfy the interface.
 */
public interface AttributeMap extends Map<String, AttributeValue> {

    AttributeValue put(String key, String value);
    AttributeValue put(String key, int value);
    AttributeValue put(String key, long value);
    AttributeValue put(String key, double value);
    AttributeValue put(String key, boolean value);
    AttributeValue put(String key, byte[] value);

    @Override
    default AttributeValue compute(String key, java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        java.util.Objects.requireNonNull(key, "Attribute key cannot be null");
        java.util.Objects.requireNonNull(remappingFunction, "Remapping function cannot be null");

        AttributeValue oldValue = get(key);
        AttributeValue newValue = remappingFunction.apply(key, oldValue);

        if (newValue == null) {
            if (oldValue != null || containsKey(key)) {
                remove(key);
                return null;
            } else {
                return null;
            }
        } else {
            put(key, newValue);
            return newValue;
        }
    }

    @Override
    default AttributeValue computeIfAbsent(String key, java.util.function.Function<? super String, ? extends AttributeValue> mappingFunction) {
        java.util.Objects.requireNonNull(key, "Attribute key cannot be null");
        java.util.Objects.requireNonNull(mappingFunction, "Mapping function cannot be null");

        AttributeValue existing = get(key);
        if (existing != null) {
            return existing;
        }

        AttributeValue computed = mappingFunction.apply(key);
        if (computed != null) {
            put(key, computed);
            return computed;
        }
        return null;
    }

    @Override
    default AttributeValue computeIfPresent(String key, java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        java.util.Objects.requireNonNull(key, "Attribute key cannot be null");
        java.util.Objects.requireNonNull(remappingFunction, "Remapping function cannot be null");

        AttributeValue oldValue = get(key);
        if (oldValue != null) {
            AttributeValue newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                remove(key);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    default AttributeValue merge(String key, AttributeValue value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        java.util.Objects.requireNonNull(key, "Attribute key cannot be null");
        java.util.Objects.requireNonNull(value, "Attribute value cannot be null");
        java.util.Objects.requireNonNull(remappingFunction, "Remapping function cannot be null");

        AttributeValue oldValue = get(key);
        AttributeValue newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);

        if (newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }

    @Override
    default void replaceAll(java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> function) {
        java.util.Objects.requireNonNull(function, "Function cannot be null");

        boolean requiresFallback = false;
        java.util.Map<String, AttributeValue> fallbackUpdates = null;

        for (Map.Entry<String, AttributeValue> entry : entrySet()) {
            String k = entry.getKey();
            AttributeValue v = entry.getValue();
            AttributeValue newValue = function.apply(k, v);
            java.util.Objects.requireNonNull(newValue, "Attribute value cannot be null");

            if (requiresFallback) {
                fallbackUpdates.put(k, newValue);
            } else {
                try {
                    entry.setValue(newValue);
                } catch (UnsupportedOperationException e) {
                    requiresFallback = true;
                    fallbackUpdates = new java.util.HashMap<>();
                    fallbackUpdates.put(k, newValue);
                }
            }
        }

        // Apply fallback updates strictly AFTER the iterator has closed
        if (requiresFallback) {
            putAll(fallbackUpdates);
        }
    }
}
