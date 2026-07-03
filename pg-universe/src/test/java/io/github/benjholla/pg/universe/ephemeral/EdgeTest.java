package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class EdgeTest {

    private Node fromNode;
    private Node toNode;
    private Edge edge;

    @BeforeEach
    public void setUp() {
        fromNode = new EphemeralNode();
        toNode = new EphemeralNode();
        edge = new EphemeralEdge(fromNode, toNode);
    }

    @Test
    public void testFromAndTo() {
        assertEquals(fromNode, edge.from());
        assertEquals(toNode, edge.to());
    }

    @Test
    public void testNullEndpoints() {
        assertThrows(IllegalArgumentException.class, () -> new EphemeralEdge(null, toNode));
        assertThrows(IllegalArgumentException.class, () -> new EphemeralEdge(fromNode, null));
        assertThrows(IllegalArgumentException.class, () -> new EphemeralEdge(null, null));
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
