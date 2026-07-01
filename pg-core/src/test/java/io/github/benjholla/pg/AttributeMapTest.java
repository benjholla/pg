package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

}
