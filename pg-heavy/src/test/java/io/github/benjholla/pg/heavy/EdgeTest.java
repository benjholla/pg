package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EdgeTest {

    private Node fromNode;
    private Node toNode;
    private Edge edge;

    @BeforeEach
    public void setUp() {
        fromNode = new HeavyNode();
        toNode = new HeavyNode();
        edge = new HeavyEdge(fromNode, toNode);
    }

    @Test
    public void testFromAndTo() {
        assertEquals(fromNode, edge.from());
        assertEquals(toNode, edge.to());
    }

    @Test
    public void testNullEndpoints() {
        assertThrows(IllegalArgumentException.class, () -> new HeavyEdge(null, toNode));
        assertThrows(IllegalArgumentException.class, () -> new HeavyEdge(fromNode, null));
        assertThrows(IllegalArgumentException.class, () -> new HeavyEdge(null, null));
    }

    @Test
    public void testToString() {
        edge.attributes().put("weight", 10);
        edge.tags().add("connection");

        String str = edge.toString();
        assertTrue(str.startsWith("HeavyEdge [from="));
        assertTrue(str.contains("to="));
        assertTrue(str.contains("attributes="));
        assertTrue(str.contains("weight=IntVal[value=10]"));
        assertTrue(str.contains("tags="));
        assertTrue(str.contains("connection"));
        assertTrue(str.endsWith("]"));
    }
}
