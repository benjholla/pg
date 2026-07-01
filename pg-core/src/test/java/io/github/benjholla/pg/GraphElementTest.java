package io.github.benjholla.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GraphElementTest {

    private static class TestGraphElement extends GraphElement {
        // Concrete class for testing the abstract GraphElement
    }

    private GraphElement element;

    @BeforeEach
    public void setUp() {
        element = new TestGraphElement();
    }

    @Test
    public void testId() {
        assertNotNull(element.id());
        GraphElement element2 = new TestGraphElement();
        assertNotEquals(element.id(), element2.id());
    }

    @Test
    public void testTags() {
        TagSet tags = element.tags();
        assertNotNull(tags);
        assertTrue(tags.isEmpty());

        tags.add("test-tag");
        assertTrue(element.tags().contains("test-tag"));
    }

    @Test
    public void testAttributes() {
        Map<String, AttributeValue> attrs = element.attributes();
        assertNotNull(attrs);
        assertTrue(attrs.isEmpty());
    }

    @Test
    public void testPutAndGetAttr() {
        assertNull(element.attributes().get("key1"));
        assertFalse(element.attributes().containsKey("key1"));

        element.attributes().put("key1", "val1");

        assertTrue(element.attributes().containsKey("key1"));
        assertEquals(new AttributeValue.StringVal("val1"), element.attributes().get("key1"));

        element.attributes().put("key1", "val2");
        assertEquals(new AttributeValue.StringVal("val2"), element.attributes().get("key1"));
    }

    @Test
    public void testRemoveAttr() {
        element.attributes().put("key1", "val1");
        assertTrue(element.attributes().containsKey("key1"));

        AttributeValue removed = element.attributes().remove("key1");
        assertEquals(new AttributeValue.StringVal("val1"), removed);
        assertFalse(element.attributes().containsKey("key1"));
        assertNull(element.attributes().get("key1"));
    }

    @Test
    public void testEqualsAndHashCode() {
        GraphElement element2 = new TestGraphElement();
        assertNotEquals(element, element2);

        // Element ID dictates equality
        assertEquals(element, element);
        assertEquals(element.hashCode(), element.hashCode());
        assertNotEquals(element.hashCode(), element2.hashCode());
        assertNotEquals(null, element);
        assertNotEquals(element, new Object());
    }
}
