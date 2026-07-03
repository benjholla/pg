package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Node;

public class NodeTest {

    @Test
    public void testToString() {
        Node node = new EphemeralNode();
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
