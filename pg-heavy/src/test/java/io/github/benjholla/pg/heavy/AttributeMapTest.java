package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.AttributeMap;
import io.github.benjholla.pg.api.AttributeValue;

public class AttributeMapTest {

    private AttributeMap attributeMap;

    @BeforeEach
    public void setUp() {
        attributeMap = new HeavyAttributeMap();
    }

    @Test
    public void testPut() {
        assertNull(attributeMap.put("key1", "value1"));
        assertEquals(new AttributeValue.StringVal("value1"), attributeMap.get("key1"));

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
        map.put("key1", new AttributeValue.StringVal("value1"));
        map.put("key2", new AttributeValue.StringVal("value2"));

        attributeMap.putAll(map);
        assertEquals(new AttributeValue.StringVal("value1"), attributeMap.get("key1"));
        assertEquals(new AttributeValue.StringVal("value2"), attributeMap.get("key2"));

        Map<String, AttributeValue> nullKeyMap = new HashMap<>();
        nullKeyMap.put(null, new AttributeValue.StringVal("value"));
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
        assertNull(attributeMap.putIfAbsent("key1", new AttributeValue.StringVal("value1")));
        assertEquals(new AttributeValue.StringVal("value1"), attributeMap.get("key1"));

        assertEquals(new AttributeValue.StringVal("value1"), attributeMap.putIfAbsent("key1", new AttributeValue.StringVal("value2")));
        assertEquals(new AttributeValue.StringVal("value1"), attributeMap.get("key1"));

        assertThrows(NullPointerException.class, () -> {
            attributeMap.putIfAbsent(null, new AttributeValue.StringVal("value"));
        });

        assertThrows(NullPointerException.class, () -> {
            attributeMap.putIfAbsent("key", null);
        });
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("key1", new AttributeValue.StringVal("value1"));

        AttributeMap attrMap = new HeavyAttributeMap(map);
        assertEquals(new AttributeValue.StringVal("value1"), attrMap.get("key1"));

        Map<String, AttributeValue> nullKeyMap = new HashMap<>();
        nullKeyMap.put(null, new AttributeValue.StringVal("value"));
        assertThrows(NullPointerException.class, () -> {
            new HeavyAttributeMap(nullKeyMap);
        });
    }



    @Test
    public void testPutPrimitives() {
        assertNull(attributeMap.put("keyInt", 42));
        assertEquals(new AttributeValue.IntVal(42), attributeMap.get("keyInt"));

        assertNull(attributeMap.put("keyLong", 42L));
        assertEquals(new AttributeValue.LongVal(42L), attributeMap.get("keyLong"));

        assertNull(attributeMap.put("keyDouble", 42.0));
        assertEquals(new AttributeValue.DoubleVal(42.0), attributeMap.get("keyDouble"));

        assertNull(attributeMap.put("keyBoolean", true));
        assertEquals(new AttributeValue.BooleanVal(true), attributeMap.get("keyBoolean"));

        byte[] bytes = new byte[]{1, 2, 3};
        assertNull(attributeMap.put("keyByteArray", bytes));
        assertEquals(new AttributeValue.ByteArrayVal(bytes), attributeMap.get("keyByteArray"));
    }

    @Test
    public void testByteArrayValEqualsAndHashCode() {
        byte[] bytes1 = new byte[]{1, 2, 3};
        byte[] bytes2 = new byte[]{1, 2, 3};
        byte[] bytes3 = new byte[]{1, 2, 4};

        AttributeValue.ByteArrayVal val1 = new AttributeValue.ByteArrayVal(bytes1);
        AttributeValue.ByteArrayVal val2 = new AttributeValue.ByteArrayVal(bytes2);
        AttributeValue.ByteArrayVal val3 = new AttributeValue.ByteArrayVal(bytes3);

        assertEquals(val1, val1);
        assertEquals(val1, val2);
        assertNotEquals(val1, val3);
        assertNotEquals(val1, null);
        assertNotEquals(val1, new Object());

        assertEquals(val1.hashCode(), val2.hashCode());
        assertNotEquals(val1.hashCode(), val3.hashCode());
    }
}
