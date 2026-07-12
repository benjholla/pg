package dev.chpg.pg.global;

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

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

public class EdgeTest {

    private Node fromNode;
    private Node toNode;
    private Edge edge;

    @BeforeEach
    public void setUp() {
        fromNode = new GlobalNode();
        toNode = new GlobalNode();
        edge = new GlobalEdge(fromNode, toNode);
    }

    @Test
    public void testId() {
        assertNotNull(edge.id());
        Edge element2 = new GlobalEdge(fromNode, toNode);
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
        assertEquals(AttributeValue.value("val1"), edge.attributes().get("key1"));

        edge.attributes().put("key1", "val2");
        assertEquals(AttributeValue.value("val2"), edge.attributes().get("key1"));
    }

    @Test
    public void testRemoveAttr() {
        edge.attributes().put("key1", "val1");
        assertTrue(edge.attributes().containsKey("key1"));

        AttributeValue removed = edge.attributes().remove("key1");
        assertEquals(AttributeValue.value("val1"), removed);
        assertFalse(edge.attributes().containsKey("key1"));
        assertNull(edge.attributes().get("key1"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Edge element2 = new GlobalEdge(fromNode, toNode);
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
        Edge el = new GlobalEdge(fromNode, toNode);
        assertFalse(el.equals(new Object()));
    }

    @Test
    public void testEqualsNull() {
        Edge el = new GlobalEdge(fromNode, toNode);
        assertFalse(el.equals(null));
    }

    @Test
    public void testFromAndTo() {
        assertEquals(fromNode, edge.from());
        assertEquals(toNode, edge.to());
    }

    @Test
    public void testNullEndpoints() {
        assertThrows(IllegalArgumentException.class, () -> new GlobalEdge(null, toNode));
        assertThrows(IllegalArgumentException.class, () -> new GlobalEdge(fromNode, null));
        assertThrows(IllegalArgumentException.class, () -> new GlobalEdge(null, null));
    }

    @Test
    public void testToString() {
        edge.attributes().put("weight", 10);
        edge.tags().add("connection");

        String str = edge.toString();
        assertTrue(str.startsWith("GlobalEdge [from="));
        assertTrue(str.contains("to="));
        assertTrue(str.contains("attributes="));
        assertTrue(str.contains("weight=IntegerValue[value=10]"));
        assertTrue(str.contains("tags="));
        assertTrue(str.contains("connection"));
        assertTrue(str.endsWith("]"));
    }
}
