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
        throw new UnsupportedOperationException();
    }

    @Override
    default AttributeValue computeIfAbsent(String key, java.util.function.Function<? super String, ? extends AttributeValue> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    default AttributeValue computeIfPresent(String key, java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    default AttributeValue merge(String key, AttributeValue value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void replaceAll(java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> function) {
        throw new UnsupportedOperationException();
    }
}
