package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

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
        assertNotNull(element.getId());
        GraphElement element2 = new TestGraphElement();
        assertNotEquals(element.getId(), element2.getId());
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
        Map<String, Object> attrs = element.attributes();
        assertNotNull(attrs);
        assertTrue(attrs.isEmpty());
    }

    @Test
    public void testPutAndGetAttr() {
        assertNull(element.getAttr("key1"));
        assertFalse(element.hasAttr("key1"));

        element.putAttr("key1", "val1");

        assertTrue(element.hasAttr("key1"));
        assertEquals("val1", element.getAttr("key1"));

        element.putAttr("key1", "val2");
        assertEquals("val2", element.getAttr("key1"));
    }

    @Test
    public void testRemoveAttr() {
        element.putAttr("key1", "val1");
        assertTrue(element.hasAttr("key1"));

        Object removed = element.removeAttr("key1");
        assertEquals("val1", removed);
        assertFalse(element.hasAttr("key1"));
        assertNull(element.getAttr("key1"));
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
