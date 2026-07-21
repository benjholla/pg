package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AttributeValueTest {

    @Test
    public void testStringValue() {
        AttributeValue.StringValue v1 = AttributeValue.value("test");
        AttributeValue.StringValue v2 = AttributeValue.value("test");
        AttributeValue.StringValue v3 = AttributeValue.value("other");

        assertEquals("test", v1.value());
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
    }

    @Test
    public void testBooleanValue() {
        AttributeValue.BooleanValue v1 = AttributeValue.value(true);
        AttributeValue.BooleanValue v2 = AttributeValue.value(true);
        AttributeValue.BooleanValue v3 = AttributeValue.value(false);

        assertTrue(v1.value());
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
    }

    @Test
    public void testIntegerValue() {
        AttributeValue.IntegerValue v1 = AttributeValue.value(42);
        AttributeValue.IntegerValue v2 = AttributeValue.value(42);
        AttributeValue.IntegerValue v3 = AttributeValue.value(100);

        assertEquals(42, v1.value());
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
    }

    @Test
    public void testLongValue() {
        AttributeValue.LongValue v1 = AttributeValue.value(42L);
        AttributeValue.LongValue v2 = AttributeValue.value(42L);
        AttributeValue.LongValue v3 = AttributeValue.value(100L);

        assertEquals(42L, v1.value());
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
    }

    @Test
    public void testDoubleValue() {
        AttributeValue.DoubleValue v1 = AttributeValue.value(3.14);
        AttributeValue.DoubleValue v2 = AttributeValue.value(3.14);
        AttributeValue.DoubleValue v3 = AttributeValue.value(2.71);

        assertEquals(3.14, v1.value());
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
    }

    @Test
    public void testByteArrayValue() {
        byte[] arr1 = new byte[]{1, 2, 3};
        byte[] arr2 = new byte[]{1, 2, 3};
        byte[] arr3 = new byte[]{4, 5, 6};

        AttributeValue.ByteArrayValue v1 = AttributeValue.value(arr1);
        AttributeValue.ByteArrayValue v2 = AttributeValue.value(arr2);
        AttributeValue.ByteArrayValue v3 = AttributeValue.value(arr3);

        assertEquals(v1, v1); // Identity
        assertEquals(v1, v2); // Arrays.equals logic
        assertNotEquals(v1, v3);
        assertNotEquals(v1, null);
        assertNotEquals(v1, new Object());

        assertEquals(v1.hashCode(), v2.hashCode());
    }
}
