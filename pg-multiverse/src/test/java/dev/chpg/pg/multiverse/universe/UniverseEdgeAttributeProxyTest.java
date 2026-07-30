package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.chpg.pg.api.AttributeValue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UniverseEdgeAttributeProxyTest {

    private Universe universe;
    private int edgeId;
    private UniverseEdgeAttributeProxy attributes;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        edgeId = universe.idGenerator().createEdgeId();
        attributes = new UniverseEdgeAttributeProxy(universe, edgeId);
    }

    @Test
    public void testInitialization() {
        assertEquals(0, attributes.size());
        assertSame(universe, attributes.universe());
    }

    @Test
    public void testPutAndGet() {
        AttributeValue val = AttributeValue.value("Alice");
        assertNull(attributes.put("name", val));
        assertTrue(attributes.containsKey("name"));
        assertEquals(val, attributes.get("name"));
        assertEquals(1, attributes.size());

        AttributeValue newVal = AttributeValue.value("Bob");
        assertEquals(val, attributes.put("name", newVal)); // Should return old value
        assertEquals(newVal, attributes.get("name"));
        assertEquals(1, attributes.size());
    }

    @Test
    public void testPrimitiveOverloads() {
        attributes.put("str", "value");
        attributes.put("int", 123);
        attributes.put("long", 123L);
        attributes.put("double", 1.23);
        attributes.put("boolean", true);
        attributes.put("bytes", new byte[]{1, 2, 3});

        assertEquals("value", ((AttributeValue.StringValue) attributes.get("str")).value());
        assertEquals(123, ((AttributeValue.IntegerValue) attributes.get("int")).value());
        assertEquals(123L, ((AttributeValue.LongValue) attributes.get("long")).value());
        assertEquals(1.23, ((AttributeValue.DoubleValue) attributes.get("double")).value());
        assertTrue(((AttributeValue.BooleanValue) attributes.get("boolean")).value());
        assertEquals(3, ((AttributeValue.ByteArrayValue) attributes.get("bytes")).value().length);
    }

    @Test
    public void testContainsKeyAndGetWrongType() {
        assertFalse(attributes.containsKey(123));
        assertFalse(attributes.containsKey(null));
        assertNull(attributes.get(123));
        assertNull(attributes.get(null));
    }

    @Test
    public void testRemove() {
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");

        AttributeValue removed = attributes.remove("k1");
        assertEquals("v1", ((AttributeValue.StringValue) removed).value());
        assertFalse(attributes.containsKey("k1"));
        assertTrue(attributes.containsKey("k2"));
        assertEquals(1, attributes.size());

        assertNull(attributes.remove("k1")); // Already removed
        assertNull(attributes.remove(123)); // Wrong type
    }

    @Test
    public void testClear() {
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");
        assertEquals(2, attributes.size());

        attributes.clear();
        assertEquals(0, attributes.size());
        assertFalse(attributes.containsKey("k1"));
        assertFalse(attributes.containsKey("k2"));
    }

    @Test
    public void testEntrySet() {
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");

        int count = 0;
        boolean hasK1 = false;
        boolean hasK2 = false;

        for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
            if (entry.getKey().equals("k1") && ((AttributeValue.StringValue) entry.getValue()).value().equals("v1")) {
                hasK1 = true;
            }
            if (entry.getKey().equals("k2") && ((AttributeValue.StringValue) entry.getValue()).value().equals("v2")) {
                hasK2 = true;
            }
            count++;
        }

        assertEquals(2, count);
        assertTrue(hasK1);
        assertTrue(hasK2);
    }

    @Test
    public void testEntrySetIteratorFailFast() {
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");

        Iterator<Map.Entry<String, AttributeValue>> it = attributes.entrySet().iterator();
        assertTrue(it.hasNext());

        attributes.put("k3", "v3"); // modify while iterating

        assertThrows(ConcurrentModificationException.class, it::hasNext);
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    public void testNullInputs() {
        assertThrows(NullPointerException.class, () -> attributes.put(null, AttributeValue.value("val")));
        assertThrows(NullPointerException.class, () -> attributes.put("key", (AttributeValue) null));
        assertThrows(NullPointerException.class, () -> new UniverseEdgeAttributeProxy(null, edgeId));
    }
}
