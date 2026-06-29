package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EdgeTest {

    private Node fromNode;
    private Node toNode;
    private Edge edge;

    @BeforeEach
    public void setUp() {
        fromNode = new Node();
        toNode = new Node();
        edge = new Edge(fromNode, toNode);
    }

    @Test
    public void testFromAndTo() {
        assertEquals(fromNode, edge.from());
        assertEquals(toNode, edge.to());
    }

    @Test
    public void testToString() {
        edge.putAttr("weight", 10);
        edge.tags().add("connection");

        String str = edge.toString();
        assertTrue(str.startsWith("Edge [from="));
        assertTrue(str.contains("to="));
        assertTrue(str.contains("attributes="));
        assertTrue(str.contains("weight=10"));
        assertTrue(str.contains("tags="));
        assertTrue(str.contains("connection"));
        assertTrue(str.endsWith("]"));
    }
}
