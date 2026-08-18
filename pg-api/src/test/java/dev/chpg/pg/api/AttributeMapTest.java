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
    public void testMergeDefaultMethods() {
        // String
        assertEquals(AttributeValue.value("test"), map.merge("k_str", "test", (oldV, newV) -> newV));
        assertEquals(AttributeValue.value("test"), map.get("k_str"));
        assertEquals(AttributeValue.value("other"), map.merge("k_str", "other", (oldV, newV) -> newV));

        // int
        assertEquals(AttributeValue.value(1), map.merge("k_int", 1, (oldV, newV) -> newV));
        assertEquals(AttributeValue.value(1), map.get("k_int"));
        assertEquals(AttributeValue.value(2), map.merge("k_int", 2, (oldV, newV) -> newV));

        // long
        assertEquals(AttributeValue.value(1L), map.merge("k_long", 1L, (oldV, newV) -> newV));
        assertEquals(AttributeValue.value(1L), map.get("k_long"));
        assertEquals(AttributeValue.value(2L), map.merge("k_long", 2L, (oldV, newV) -> newV));

        // double
        assertEquals(AttributeValue.value(1.5), map.merge("k_double", 1.5, (oldV, newV) -> newV));
        assertEquals(AttributeValue.value(1.5), map.get("k_double"));
        assertEquals(AttributeValue.value(2.5), map.merge("k_double", 2.5, (oldV, newV) -> newV));

        // boolean
        assertEquals(AttributeValue.value(true), map.merge("k_bool", true, (oldV, newV) -> newV));
        assertEquals(AttributeValue.value(true), map.get("k_bool"));
        assertEquals(AttributeValue.value(false), map.merge("k_bool", false, (oldV, newV) -> newV));

        // byte[]
        byte[] bytes1 = new byte[]{1};
        byte[] bytes2 = new byte[]{2};
        assertEquals(AttributeValue.value(bytes1), map.merge("k_bytes", bytes1, (oldV, newV) -> newV));
        assertEquals(AttributeValue.value(bytes1), map.get("k_bytes"));
        assertEquals(AttributeValue.value(bytes2), map.merge("k_bytes", bytes2, (oldV, newV) -> newV));
    }

    @Test
    public void testMerge() {
        assertThrows(NullPointerException.class, () -> map.merge(null, AttributeValue.value(1), (oldV, newV) -> newV));
        assertThrows(NullPointerException.class, () -> map.merge("key", (AttributeValue) null, (oldV, newV) -> newV));
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

    @Test
    public void testPutDefaultMethods() {
        assertNull(map.put("k_str", "test"));
        assertEquals(AttributeValue.value("test"), map.get("k_str"));
        assertEquals(AttributeValue.value("test"), map.put("k_str", "other"));
        assertEquals(AttributeValue.value("other"), map.get("k_str"));

        assertNull(map.put("k_int", 1));
        assertEquals(AttributeValue.value(1), map.get("k_int"));
        assertEquals(AttributeValue.value(1), map.put("k_int", 2));
        assertEquals(AttributeValue.value(2), map.get("k_int"));

        assertNull(map.put("k_long", 1L));
        assertEquals(AttributeValue.value(1L), map.get("k_long"));
        assertEquals(AttributeValue.value(1L), map.put("k_long", 2L));
        assertEquals(AttributeValue.value(2L), map.get("k_long"));

        assertNull(map.put("k_double", 1.5));
        assertEquals(AttributeValue.value(1.5), map.get("k_double"));
        assertEquals(AttributeValue.value(1.5), map.put("k_double", 2.5));
        assertEquals(AttributeValue.value(2.5), map.get("k_double"));

        assertNull(map.put("k_bool", true));
        assertEquals(AttributeValue.value(true), map.get("k_bool"));
        assertEquals(AttributeValue.value(true), map.put("k_bool", false));
        assertEquals(AttributeValue.value(false), map.get("k_bool"));

        byte[] bytes1 = new byte[]{1};
        byte[] bytes2 = new byte[]{2};
        assertNull(map.put("k_bytes", bytes1));
        assertEquals(AttributeValue.value(bytes1), map.get("k_bytes"));
        assertEquals(AttributeValue.value(bytes1), map.put("k_bytes", bytes2));
        assertEquals(AttributeValue.value(bytes2), map.get("k_bytes"));
    }

    @Test
    public void testPutIfAbsentDefaultMethods() {
        assertNull(map.putIfAbsent("k_str", "test"));
        assertEquals(AttributeValue.value("test"), map.get("k_str"));
        assertEquals(AttributeValue.value("test"), map.putIfAbsent("k_str", "other"));

        assertNull(map.putIfAbsent("k_int", 1));
        assertEquals(AttributeValue.value(1), map.get("k_int"));
        assertEquals(AttributeValue.value(1), map.putIfAbsent("k_int", 2));

        assertNull(map.putIfAbsent("k_long", 1L));
        assertEquals(AttributeValue.value(1L), map.get("k_long"));
        assertEquals(AttributeValue.value(1L), map.putIfAbsent("k_long", 2L));

        assertNull(map.putIfAbsent("k_double", 1.5));
        assertEquals(AttributeValue.value(1.5), map.get("k_double"));
        assertEquals(AttributeValue.value(1.5), map.putIfAbsent("k_double", 2.5));

        assertNull(map.putIfAbsent("k_bool", true));
        assertEquals(AttributeValue.value(true), map.get("k_bool"));
        assertEquals(AttributeValue.value(true), map.putIfAbsent("k_bool", false));

        byte[] bytes1 = new byte[]{1};
        byte[] bytes2 = new byte[]{2};
        assertNull(map.putIfAbsent("k_bytes", bytes1));
        assertEquals(AttributeValue.value(bytes1), map.get("k_bytes"));
        assertEquals(AttributeValue.value(bytes1), map.putIfAbsent("k_bytes", bytes2));
    }

    // A class that delegates compute/merge/replaceAll etc. to the default interface methods.
    private static class TestAttributeMap extends HashMap<String, AttributeValue> implements AttributeMap {

        private static final long serialVersionUID = 1L;

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
        private static final long serialVersionUID = 1L;

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
