package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    @Test
    public void testToString() {
        Node node = new HeavyNode();
        node.attributes().put("name", "test-node");
        node.tags().add("test-tag");

        String str = node.toString();
        assertTrue(str.startsWith("HeavyNode ["));
        assertTrue(str.contains("attributes="));
        assertTrue(str.contains("name=StringVal[value=test-node]"));
        assertTrue(str.contains("tags="));
        assertTrue(str.contains("test-tag"));
        assertTrue(str.endsWith("]"));
    }
}
