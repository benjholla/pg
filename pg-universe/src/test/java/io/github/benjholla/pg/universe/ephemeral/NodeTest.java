package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.TagSet;

public class NodeTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    private Node element;

    @BeforeEach
    public void setUp() {
        element = factory.createNode();
    }

    @Test
    public void testId() {
        assertNotNull(element.id());
        Node element2 = factory.createNode();
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
        Node element2 = factory.createNode();
        assertNotEquals(element, element2);

        // Element ID dictates equality
        assertEquals(element, element);
        assertEquals(element.hashCode(), element.hashCode());
        assertNotEquals(element.hashCode(), element2.hashCode());
        assertNotEquals(null, element);
        assertNotEquals(element, new Object());
    }

    @Test
    public void testEqualsDifferentClass2() {
        Node el = factory.createNode();
        assertFalse(el.equals(new Object()));
    }

    @Test
    public void testEqualsNull() {
        Node el = factory.createNode();
        assertFalse(el.equals(null));
    }

    @Test
    public void testToString() {
        Node node = factory.createNode();
        node.attributes().put("name", "test-node");
        node.tags().add("test-tag");

        String str = node.toString();
        assertTrue(str.startsWith("EphemeralNode ["));
        assertTrue(str.contains("attributes="));
        assertTrue(str.contains("name=StringVal[value=test-node]"));
        assertTrue(str.contains("tags="));
        assertTrue(str.contains("test-tag"));
        assertTrue(str.endsWith("]"));
    }
}
