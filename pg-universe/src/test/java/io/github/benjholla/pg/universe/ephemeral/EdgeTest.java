package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.TagSet;

public class EdgeTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    private Node fromNode;
    private Node toNode;
    private Edge edge;

    @BeforeEach
    public void setUp() {
        fromNode = factory.createNode();
        toNode = factory.createNode();
        edge = factory.createEdge(fromNode, toNode);
    }

    @Test
    public void testId() {
        assertNotNull(edge.id());
        Edge element2 = factory.createEdge(fromNode, toNode);
        assertNotEquals(edge.id(), element2.id());
    }

    @Test
    public void testTags() {
        TagSet tags = edge.tags();
        assertNotNull(tags);
        assertTrue(tags.isEmpty());

        tags.add("test-tag");
        assertTrue(edge.tags().contains("test-tag"));
    }

    @Test
    public void testAttributes() {
        Map<String, AttributeValue> attrs = edge.attributes();
        assertNotNull(attrs);
        assertTrue(attrs.isEmpty());
    }

    @Test
    public void testPutAndGetAttr() {
        assertNull(edge.attributes().get("key1"));
        assertFalse(edge.attributes().containsKey("key1"));

        edge.attributes().put("key1", "val1");

        assertTrue(edge.attributes().containsKey("key1"));
        assertEquals(new AttributeValue.StringVal("val1"), edge.attributes().get("key1"));

        edge.attributes().put("key1", "val2");
        assertEquals(new AttributeValue.StringVal("val2"), edge.attributes().get("key1"));
    }

    @Test
    public void testRemoveAttr() {
        edge.attributes().put("key1", "val1");
        assertTrue(edge.attributes().containsKey("key1"));

        AttributeValue removed = edge.attributes().remove("key1");
        assertEquals(new AttributeValue.StringVal("val1"), removed);
        assertFalse(edge.attributes().containsKey("key1"));
        assertNull(edge.attributes().get("key1"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Edge element2 = factory.createEdge(fromNode, toNode);
        assertNotEquals(edge, element2);

        // Element ID dictates equality
        assertEquals(edge, edge);
        assertEquals(edge.hashCode(), edge.hashCode());
        assertNotEquals(edge.hashCode(), element2.hashCode());
        assertNotEquals(null, edge);
        assertNotEquals(edge, new Object());
    }

    @Test
    public void testEqualsDifferentClass2() {
        Edge el = factory.createEdge(fromNode, toNode);
        assertFalse(el.equals(new Object()));
    }

    @Test
    public void testEqualsNull() {
        Edge el = factory.createEdge(fromNode, toNode);
        assertFalse(el.equals(null));
    }

    @Test
    public void testFromAndTo() {
        assertEquals(fromNode, edge.from());
        assertEquals(toNode, edge.to());
    }

    @Test
    public void testNullEndpoints() {
        assertThrows(IllegalArgumentException.class, () -> factory.createEdge(null, toNode));
        assertThrows(IllegalArgumentException.class, () -> factory.createEdge(fromNode, null));
        assertThrows(IllegalArgumentException.class, () -> factory.createEdge(null, null));
    }

    @Test
    public void testToString() {
        edge.attributes().put("weight", 10);
        edge.tags().add("connection");

        String str = edge.toString();
        assertTrue(str.startsWith("EphemeralEdge [from="));
        assertTrue(str.contains("to="));
        assertTrue(str.contains("attributes="));
        assertTrue(str.contains("weight=IntVal[value=10]"));
        assertTrue(str.contains("tags="));
        assertTrue(str.contains("connection"));
        assertTrue(str.endsWith("]"));
    }
}
