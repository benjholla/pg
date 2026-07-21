package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttributeMapTest {

    private AttributeMap map;

    @BeforeEach
    public void setUp() {
        map = new TestAttributeMap();
    }

    @Test
    public void testCompute() {
        // According to the code, compute throws NullPointerException manually via Objects.requireNonNull
        assertThrows(NullPointerException.class, () -> map.compute(null, (k, v) -> AttributeValue.value(1)));
        assertThrows(NullPointerException.class, () -> map.compute("key", null));

        // Insert
        AttributeValue val = map.compute("key", (k, v) -> AttributeValue.value(1));
        assertEquals(AttributeValue.value(1), val);
        assertEquals(AttributeValue.value(1), map.get("key"));

        // Update
        AttributeValue val2 = map.compute("key", (k, v) -> AttributeValue.value(2));
        assertEquals(AttributeValue.value(2), val2);
        assertEquals(AttributeValue.value(2), map.get("key"));

        // Remove
        AttributeValue val3 = map.compute("key", (k, v) -> null);
        assertNull(val3);
        assertNull(map.get("key"));

        // Remove non-existent
        AttributeValue val4 = map.compute("nonexistent", (k, v) -> null);
        assertNull(val4);
    }

    @Test
    public void testComputeIfAbsent() {
        assertThrows(NullPointerException.class, () -> map.computeIfAbsent(null, k -> AttributeValue.value(1)));
        assertThrows(NullPointerException.class, () -> map.computeIfAbsent("key", null));

        // Insert
        AttributeValue val = map.computeIfAbsent("key", k -> AttributeValue.value(1));
        assertEquals(AttributeValue.value(1), val);
        assertEquals(AttributeValue.value(1), map.get("key"));

        // No-op if present
        AttributeValue val2 = map.computeIfAbsent("key", k -> AttributeValue.value(2));
        assertEquals(AttributeValue.value(1), val2);
        assertEquals(AttributeValue.value(1), map.get("key"));

        // Null mapping function returns null
        AttributeValue val3 = map.computeIfAbsent("key2", k -> null);
        assertNull(val3);
        assertNull(map.get("key2"));
    }

    @Test
    public void testComputeIfPresent() {
        assertThrows(NullPointerException.class, () -> map.computeIfPresent(null, (k, v) -> AttributeValue.value(1)));
        assertThrows(NullPointerException.class, () -> map.computeIfPresent("key", null));

        // No-op if not present
        AttributeValue val = map.computeIfPresent("key", (k, v) -> AttributeValue.value(1));
        assertNull(val);
        assertNull(map.get("key"));

        // Update if present
        map.put("key", AttributeValue.value(1));
        AttributeValue val2 = map.computeIfPresent("key", (k, v) -> AttributeValue.value(2));
        assertEquals(AttributeValue.value(2), val2);
        assertEquals(AttributeValue.value(2), map.get("key"));

        // Remove if mapping function returns null
        AttributeValue val3 = map.computeIfPresent("key", (k, v) -> null);
        assertNull(val3);
        assertNull(map.get("key"));
    }

    @Test
    public void testMerge() {
        assertThrows(NullPointerException.class, () -> map.merge(null, AttributeValue.value(1), (oldV, newV) -> newV));
        assertThrows(NullPointerException.class, () -> map.merge("key", null, (oldV, newV) -> newV));
        assertThrows(NullPointerException.class, () -> map.merge("key", AttributeValue.value(1), null));

        // Insert if absent
        AttributeValue val = map.merge("key", AttributeValue.value(1), (oldV, newV) -> newV);
        assertEquals(AttributeValue.value(1), val);
        assertEquals(AttributeValue.value(1), map.get("key"));

        // Merge if present
        AttributeValue val2 = map.merge("key", AttributeValue.value(2), (oldV, newV) -> newV);
        assertEquals(AttributeValue.value(2), val2);
        assertEquals(AttributeValue.value(2), map.get("key"));

        // Remove if merge function returns null
        AttributeValue val3 = map.merge("key", AttributeValue.value(3), (oldV, newV) -> null);
        assertNull(val3);
        assertNull(map.get("key"));
    }

    @Test
    public void testReplaceAll() {
        assertThrows(NullPointerException.class, () -> map.replaceAll(null));

        map.put("key1", AttributeValue.value(1));
        map.put("key2", AttributeValue.value(2));

        map.replaceAll((k, v) -> {
            if (v instanceof AttributeValue.IntegerValue iv) {
                return AttributeValue.value(iv.value() * 2);
            }
            return v;
        });

        assertEquals(AttributeValue.value(2), map.get("key1"));
        assertEquals(AttributeValue.value(4), map.get("key2"));

        // This will trigger requireNonNull in the middle of replaceAll iteration
        assertThrows(NullPointerException.class, () -> map.replaceAll((k, v) -> null));
    }

    @Test
    public void testReplaceAllUnsupportedOperationExceptionFallback() {
        // Let's modify the map entries to throw UnsupportedOperationException on setValue
        AttributeMap wrapper = new FallbackTestAttributeMap();

        wrapper.put("k1", AttributeValue.value(10));
        wrapper.put("k2", AttributeValue.value(20));

        // This should trigger the fallback branch inside replaceAll
        wrapper.replaceAll((k, v) -> AttributeValue.value(((AttributeValue.IntegerValue)v).value() * 2));

        assertEquals(AttributeValue.value(20), wrapper.get("k1"));
        assertEquals(AttributeValue.value(40), wrapper.get("k2"));
    }

    // A class that delegates compute/merge/replaceAll etc. to the default interface methods.
    private static class TestAttributeMap extends HashMap<String, AttributeValue> implements AttributeMap {

        @Override
        public AttributeValue put(String key, String value) {
            return put(key, AttributeValue.value(value));
        }

        @Override
        public AttributeValue put(String key, int value) {
            return put(key, AttributeValue.value(value));
        }

        @Override
        public AttributeValue put(String key, long value) {
            return put(key, AttributeValue.value(value));
        }

        @Override
        public AttributeValue put(String key, double value) {
            return put(key, AttributeValue.value(value));
        }

        @Override
        public AttributeValue put(String key, boolean value) {
            return put(key, AttributeValue.value(value));
        }

        @Override
        public AttributeValue put(String key, byte[] value) {
            return put(key, AttributeValue.value(value));
        }

        @Override
        public AttributeValue compute(String key, java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
            return AttributeMap.super.compute(key, remappingFunction);
        }

        @Override
        public AttributeValue computeIfAbsent(String key, java.util.function.Function<? super String, ? extends AttributeValue> mappingFunction) {
            return AttributeMap.super.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public AttributeValue computeIfPresent(String key, java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
            return AttributeMap.super.computeIfPresent(key, remappingFunction);
        }

        @Override
        public AttributeValue merge(String key, AttributeValue value, java.util.function.BiFunction<? super AttributeValue, ? super AttributeValue, ? extends AttributeValue> remappingFunction) {
            return AttributeMap.super.merge(key, value, remappingFunction);
        }

        @Override
        public void replaceAll(java.util.function.BiFunction<? super String, ? super AttributeValue, ? extends AttributeValue> function) {
            AttributeMap.super.replaceAll(function);
        }
    }

    private static class FallbackTestAttributeMap extends TestAttributeMap {
        @Override
        public java.util.Set<java.util.Map.Entry<String, AttributeValue>> entrySet() {
            java.util.Set<java.util.Map.Entry<String, AttributeValue>> original = super.entrySet();
            return new java.util.AbstractSet<>() {
                @Override
                public java.util.Iterator<java.util.Map.Entry<String, AttributeValue>> iterator() {
                    java.util.Iterator<java.util.Map.Entry<String, AttributeValue>> origIt = original.iterator();
                    return new java.util.Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return origIt.hasNext();
                        }
                        @Override
                        public java.util.Map.Entry<String, AttributeValue> next() {
                            java.util.Map.Entry<String, AttributeValue> entry = origIt.next();
                            return new java.util.Map.Entry<>() {
                                @Override
                                public String getKey() { return entry.getKey(); }
                                @Override
                                public AttributeValue getValue() { return entry.getValue(); }
                                @Override
                                public AttributeValue setValue(AttributeValue value) {
                                    throw new UnsupportedOperationException("Intentional throw for testing fallback");
                                }
                            };
                        }
                    };
                }
                @Override
                public int size() {
                    return original.size();
                }
            };
        }
    }
}
