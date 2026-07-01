package io.github.benjholla.pg;

import java.util.Map;

/**
 * Attributes map arbitrary keys to object values.
 */
public interface AttributeMap extends Map<String, AttributeValue> {

    AttributeValue put(String key, String value);
    AttributeValue put(String key, int value);
    AttributeValue put(String key, long value);
    AttributeValue put(String key, double value);
    AttributeValue put(String key, boolean value);
    AttributeValue put(String key, byte[] value);

}
