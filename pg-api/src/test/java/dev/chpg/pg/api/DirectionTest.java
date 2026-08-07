package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class DirectionTest {

    @Test
    public void testDirectionValues() {
        assertEquals(3, Direction.values().length);

        assertNotNull(Direction.valueOf("IN"));
        assertNotNull(Direction.valueOf("OUT"));
        assertNotNull(Direction.valueOf("BOTH"));

        assertEquals(Direction.IN, Direction.valueOf("IN"));
        assertEquals(Direction.OUT, Direction.valueOf("OUT"));
        assertEquals(Direction.BOTH, Direction.valueOf("BOTH"));
    }
}
