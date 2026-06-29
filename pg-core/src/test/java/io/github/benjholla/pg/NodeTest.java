package io.github.benjholla.pg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    @Test
    public void testToString() {
        Node node = new Node();
        node.putAttr("name", "test-node");
        node.tags().add("test-tag");

        String str = node.toString();
        assertTrue(str.startsWith("Node ["));
        assertTrue(str.contains("attributes="));
        assertTrue(str.contains("name=test-node"));
        assertTrue(str.contains("tags="));
        assertTrue(str.contains("test-tag"));
        assertTrue(str.endsWith("]"));
    }
}
