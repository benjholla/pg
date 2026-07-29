package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.AttributeValue;

public class AttributeMapTest {

    private AttributeMap attributeMap;

    @BeforeEach
    public void setUp() {
        attributeMap = new EphemeralAttributeMap();
    }

    @Test
    public void testPut() {
        assertNull(attributeMap.put("key1", "value1"));
        assertEquals(AttributeValue.value("value1"), attributeMap.get("key1"));

        assertThrows(NullPointerException.class, () -> {
            attributeMap.put(null, "value");
        });

        assertThrows(NullPointerException.class, () -> {
            attributeMap.put("key", (AttributeValue) null);
        });
    }

    @Test
    public void testPutAll() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("key1", AttributeValue.value("value1"));
        map.put("key2", AttributeValue.value("value2"));

        attributeMap.putAll(map);
        assertEquals(AttributeValue.value("value1"), attributeMap.get("key1"));
        assertEquals(AttributeValue.value("value2"), attributeMap.get("key2"));

        Map<String, AttributeValue> nullKeyMap = new HashMap<>();
        nullKeyMap.put(null, AttributeValue.value("value"));
        assertThrows(NullPointerException.class, () -> {
            attributeMap.putAll(nullKeyMap);
        });

        Map<String, AttributeValue> nullValueMap = new HashMap<>();
        nullValueMap.put("key", null);
        assertThrows(NullPointerException.class, () -> {
            attributeMap.putAll(nullValueMap);
        });

        assertThrows(NullPointerException.class, () -> {
            attributeMap.putAll(null);
        });
    }

    @Test
    public void testPutIfAbsent() {
        assertNull(attributeMap.putIfAbsent("key1", AttributeValue.value("value1")));
        assertEquals(AttributeValue.value("value1"), attributeMap.get("key1"));

        assertEquals(AttributeValue.value("value1"), attributeMap.putIfAbsent("key1", AttributeValue.value("value2")));
        assertEquals(AttributeValue.value("value1"), attributeMap.get("key1"));

        assertThrows(NullPointerException.class, () -> {
            attributeMap.putIfAbsent(null, AttributeValue.value("value"));
        });

        assertThrows(NullPointerException.class, () -> {
            attributeMap.putIfAbsent("key", null);
        });
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("key1", AttributeValue.value("value1"));

        AttributeMap attrMap = new EphemeralAttributeMap(map);
        assertEquals(AttributeValue.value("value1"), attrMap.get("key1"));

        Map<String, AttributeValue> nullKeyMap = new HashMap<>();
        nullKeyMap.put(null, AttributeValue.value("value"));
        assertThrows(NullPointerException.class, () -> {
            new EphemeralAttributeMap(nullKeyMap);
        });
    }



    @Test
    public void testPutPrimitives() {
        assertNull(attributeMap.put("keyInt", 42));
        assertEquals(AttributeValue.value(42), attributeMap.get("keyInt"));

        assertNull(attributeMap.put("keyLong", 42L));
        assertEquals(AttributeValue.value(42L), attributeMap.get("keyLong"));

        assertNull(attributeMap.put("keyDouble", 42.0));
        assertEquals(AttributeValue.value(42.0), attributeMap.get("keyDouble"));

        assertNull(attributeMap.put("keyBoolean", true));
        assertEquals(AttributeValue.value(true), attributeMap.get("keyBoolean"));

        byte[] bytes = new byte[]{1, 2, 3};
        assertNull(attributeMap.put("keyByteArray", bytes));
        assertEquals(AttributeValue.value(bytes), attributeMap.get("keyByteArray"));
    }

    @Test
    public void testByteArrayValEqualsAndHashCode() {
        byte[] bytes1 = new byte[]{1, 2, 3};
        byte[] bytes2 = new byte[]{1, 2, 3};
        byte[] bytes3 = new byte[]{1, 2, 4};

        AttributeValue.ByteArrayValue val1 = AttributeValue.value(bytes1);
        AttributeValue.ByteArrayValue val2 = AttributeValue.value(bytes2);
        AttributeValue.ByteArrayValue val3 = AttributeValue.value(bytes3);

        assertEquals(val1, val1);
        assertEquals(val1, val2);
        assertNotEquals(val1, val3);
        assertNotEquals(val1, null);
        assertNotEquals(val1, new Object());

        assertEquals(val1.hashCode(), val2.hashCode());
        assertNotEquals(val1.hashCode(), val3.hashCode());
    }


    @Test
    public void testMapOperations() {
        assertEquals(0, attributeMap.size());
        attributeMap.put("key1", "value1");
        assertEquals(1, attributeMap.size());

        attributeMap.put("key2", 42);
        assertEquals(2, attributeMap.size());

        org.junit.jupiter.api.Assertions.assertTrue(attributeMap.containsValue(AttributeValue.value("value1")));
        org.junit.jupiter.api.Assertions.assertTrue(attributeMap.containsValue(AttributeValue.value(42)));
        org.junit.jupiter.api.Assertions.assertFalse(attributeMap.containsValue(AttributeValue.value("missing")));

        java.util.Set<String> keys = attributeMap.keySet();
        org.junit.jupiter.api.Assertions.assertTrue(keys.contains("key1"));
        org.junit.jupiter.api.Assertions.assertTrue(keys.contains("key2"));
        assertEquals(2, keys.size());

        java.util.Collection<AttributeValue> values = attributeMap.values();
        org.junit.jupiter.api.Assertions.assertTrue(values.contains(AttributeValue.value("value1")));
        org.junit.jupiter.api.Assertions.assertTrue(values.contains(AttributeValue.value(42)));
        assertEquals(2, values.size());

        attributeMap.clear();
        assertEquals(0, attributeMap.size());
        org.junit.jupiter.api.Assertions.assertTrue(attributeMap.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        AttributeMap map1 = new EphemeralAttributeMap();
        map1.put("key1", "value1");
        map1.put("key2", 42);

        AttributeMap map2 = new EphemeralAttributeMap();
        map2.put("key1", "value1");
        map2.put("key2", 42);

        AttributeMap map3 = new EphemeralAttributeMap();
        map3.put("key1", "value1");
        map3.put("key2", 43);

        assertEquals(map1, map1);
        assertEquals(map1, map2);
        assertNotEquals(map1, map3);
        assertNotEquals(map1, null);
        assertNotEquals(map1, new Object());

        assertEquals(map1.hashCode(), map2.hashCode());
        assertNotEquals(map1.hashCode(), map3.hashCode());
    }

    @Test
    public void testComputeMethods() {
        assertThrows(NullPointerException.class, () -> attributeMap.compute(null, (k, v) -> AttributeValue.value("v")));
        assertThrows(NullPointerException.class, () -> attributeMap.compute("k", null));

        assertThrows(NullPointerException.class, () -> attributeMap.computeIfAbsent(null, k -> AttributeValue.value("v")));
        assertThrows(NullPointerException.class, () -> attributeMap.computeIfAbsent("k", null));

        assertThrows(NullPointerException.class, () -> attributeMap.computeIfPresent(null, (k, v) -> AttributeValue.value("v")));
        assertThrows(NullPointerException.class, () -> attributeMap.computeIfPresent("k", null));

        assertThrows(NullPointerException.class, () -> attributeMap.merge(null, AttributeValue.value("v"), (v1, v2) -> AttributeValue.value("v3")));
        assertThrows(NullPointerException.class, () -> attributeMap.merge("k", null, (v1, v2) -> AttributeValue.value("v3")));
        assertThrows(NullPointerException.class, () -> attributeMap.merge("k", AttributeValue.value("v"), null));

        assertThrows(NullPointerException.class, () -> attributeMap.replaceAll(null));

        attributeMap.compute("key1", (k, v) -> AttributeValue.value("val1"));
        assertEquals(AttributeValue.value("val1"), attributeMap.get("key1"));

        attributeMap.computeIfAbsent("key2", k -> AttributeValue.value("val2"));
        assertEquals(AttributeValue.value("val2"), attributeMap.get("key2"));

        attributeMap.computeIfPresent("key1", (k, v) -> AttributeValue.value("val1-mod"));
        assertEquals(AttributeValue.value("val1-mod"), attributeMap.get("key1"));

        attributeMap.merge("key3", AttributeValue.value("val3"), (v1, v2) -> AttributeValue.value("merged"));
        assertEquals(AttributeValue.value("val3"), attributeMap.get("key3"));

        attributeMap.merge("key3", AttributeValue.value("val3-new"), (v1, v2) -> AttributeValue.value("merged"));
        assertEquals(AttributeValue.value("merged"), attributeMap.get("key3"));

        attributeMap.replaceAll((k, v) -> AttributeValue.value("replaced"));
        assertEquals(AttributeValue.value("replaced"), attributeMap.get("key1"));
        assertEquals(AttributeValue.value("replaced"), attributeMap.get("key2"));
        assertEquals(AttributeValue.value("replaced"), attributeMap.get("key3"));
    }
}
