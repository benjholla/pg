package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class NodeDirectionTest {

    @Test
    public void testNodeDirectionValues() {
        assertEquals(3, Node.NodeDirection.values().length);

        assertNotNull(Node.NodeDirection.valueOf("IN"));
        assertNotNull(Node.NodeDirection.valueOf("OUT"));
        assertNotNull(Node.NodeDirection.valueOf("BOTH"));

        assertEquals(Node.NodeDirection.IN, Node.NodeDirection.valueOf("IN"));
        assertEquals(Node.NodeDirection.OUT, Node.NodeDirection.valueOf("OUT"));
        assertEquals(Node.NodeDirection.BOTH, Node.NodeDirection.valueOf("BOTH"));
    }
}