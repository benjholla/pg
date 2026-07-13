package dev.chpg.pg.api;

import java.util.Map;

/**
 * Attributes map arbitrary keys to attribute values.
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
        for (Map.Entry<String, AttributeValue> entry : entrySet()) {
            String k = entry.getKey();
            AttributeValue v = entry.getValue();
            AttributeValue newValue = function.apply(k, v);
            java.util.Objects.requireNonNull(newValue, "Attribute value cannot be null");
            try {
                entry.setValue(newValue);
            } catch (UnsupportedOperationException e) {
                put(k, newValue);
            }
        }
    }
}
