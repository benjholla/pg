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

    /**
     * Associates the specified String value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    default AttributeValue put(String key, String value) {
        return put(key, AttributeValue.value(value));
    }

    /**
     * Associates the specified int value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    default AttributeValue put(String key, int value) {
        return put(key, AttributeValue.value(value));
    }

    /**
     * Associates the specified long value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    default AttributeValue put(String key, long value) {
        return put(key, AttributeValue.value(value));
    }

    /**
     * Associates the specified double value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    default AttributeValue put(String key, double value) {
        return put(key, AttributeValue.value(value));
    }

    /**
     * Associates the specified boolean value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    default AttributeValue put(String key, boolean value) {
        return put(key, AttributeValue.value(value));
    }

    /**
     * Associates the specified byte[] value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    default AttributeValue put(String key, byte[] value) {
        return put(key, AttributeValue.value(value));
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null)
     * associates it with the given String value and returns null, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key.
     */
    default AttributeValue putIfAbsent(String key, String value) {
        return putIfAbsent(key, AttributeValue.value(value));
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null)
     * associates it with the given int value and returns null, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key.
     */
    default AttributeValue putIfAbsent(String key, int value) {
        return putIfAbsent(key, AttributeValue.value(value));
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null)
     * associates it with the given long value and returns null, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key.
     */
    default AttributeValue putIfAbsent(String key, long value) {
        return putIfAbsent(key, AttributeValue.value(value));
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null)
     * associates it with the given double value and returns null, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key.
     */
    default AttributeValue putIfAbsent(String key, double value) {
        return putIfAbsent(key, AttributeValue.value(value));
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null)
     * associates it with the given boolean value and returns null, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key.
     */
    default AttributeValue putIfAbsent(String key, boolean value) {
        return putIfAbsent(key, AttributeValue.value(value));
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null)
     * associates it with the given byte[] value and returns null, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key.
     */
    default AttributeValue putIfAbsent(String key, byte[] value) {
        return putIfAbsent(key, AttributeValue.value(value));
    }

    /**
     * If the specified key is not already associated with a value or is associated with null,
     * associates it with the given non-null String value. Otherwise, replaces the associated value with the results
     * of the given remapping function, or removes if the result is null.
     *
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value associated with the key or, if no existing value or a null value is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no value is associated with the key
     */
    default AttributeValue merge(String key, String value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        return merge(key, AttributeValue.value(value), remappingFunction);
    }

    /**
     * If the specified key is not already associated with a value or is associated with null,
     * associates it with the given non-null int value. Otherwise, replaces the associated value with the results
     * of the given remapping function, or removes if the result is null.
     *
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value associated with the key or, if no existing value or a null value is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no value is associated with the key
     */
    default AttributeValue merge(String key, int value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        return merge(key, AttributeValue.value(value), remappingFunction);
    }

    /**
     * If the specified key is not already associated with a value or is associated with null,
     * associates it with the given non-null long value. Otherwise, replaces the associated value with the results
     * of the given remapping function, or removes if the result is null.
     *
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value associated with the key or, if no existing value or a null value is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no value is associated with the key
     */
    default AttributeValue merge(String key, long value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        return merge(key, AttributeValue.value(value), remappingFunction);
    }

    /**
     * If the specified key is not already associated with a value or is associated with null,
     * associates it with the given non-null double value. Otherwise, replaces the associated value with the results
     * of the given remapping function, or removes if the result is null.
     *
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value associated with the key or, if no existing value or a null value is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no value is associated with the key
     */
    default AttributeValue merge(String key, double value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        return merge(key, AttributeValue.value(value), remappingFunction);
    }

    /**
     * If the specified key is not already associated with a value or is associated with null,
     * associates it with the given non-null boolean value. Otherwise, replaces the associated value with the results
     * of the given remapping function, or removes if the result is null.
     *
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value associated with the key or, if no existing value or a null value is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no value is associated with the key
     */
    default AttributeValue merge(String key, boolean value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        return merge(key, AttributeValue.value(value), remappingFunction);
    }

    /**
     * If the specified key is not already associated with a value or is associated with null,
     * associates it with the given non-null byte[] value. Otherwise, replaces the associated value with the results
     * of the given remapping function, or removes if the result is null.
     *
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value associated with the key or, if no existing value or a null value is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no value is associated with the key
     */
    default AttributeValue merge(String key, byte[] value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        return merge(key, AttributeValue.value(value), remappingFunction);
    }

    @Override
    default AttributeValue compute(String key, java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        java.util.Objects.requireNonNull(key, "Attribute key cannot be null");
        java.util.Objects.requireNonNull(remappingFunction, "Remapping function cannot be null");

        AttributeValue oldValue = get(key);
        AttributeValue newValue = remappingFunction.apply(key, oldValue);

        if (newValue == null) {
            remove(key);
            return null;
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
            for (Map.Entry<String, AttributeValue> fallbackEntry : fallbackUpdates.entrySet()) {
                this.put(fallbackEntry.getKey(), fallbackEntry.getValue());
            }
        }
    }
}
