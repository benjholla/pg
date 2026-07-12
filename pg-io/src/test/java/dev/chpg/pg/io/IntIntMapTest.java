package dev.chpg.pg.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class IntIntMapTest {

    @Test
    public void testPutAndGet() {
        IntIntMap map = new IntIntMap(10);
        map.put(1, 100);
        map.put(2, 200);
        map.put(0, 0); // 0 is a valid key

        assertEquals(100, map.get(1));
        assertEquals(200, map.get(2));
        assertEquals(0, map.get(0));
        assertEquals(-1, map.get(3)); // Not found
    }

    @Test
    public void testPutOverwriteExistingKey() {
        IntIntMap map = new IntIntMap(10);
        map.put(1, 100);
        assertEquals(100, map.get(1));

        // This triggers the `keys[index] != key` branch evaluating to false,
        // stopping the while loop and overwriting.
        map.put(1, 200);
        assertEquals(200, map.get(1));
    }

    @Test
    public void testPutEmptyMarkerThrowsException() {
        IntIntMap map = new IntIntMap(10);
        assertThrows(IllegalArgumentException.class, () -> {
            map.put(-1, 100); // -1 is the EMPTY marker
        });
    }

    @Test
    public void testHashCollisionLinearProbing() {
        IntIntMap map = new IntIntMap(2); // very small capacity

        // Let's brute force some collisions. Since capacity is a power of 2,
        // the mask will be (capacity - 1).
        // By inserting many values, we are guaranteed to have linear probing occur.
        map.put(10, 100);
        map.put(20, 200);
        map.put(30, 300);

        assertEquals(100, map.get(10));
        assertEquals(200, map.get(20));
        assertEquals(300, map.get(30));

        // Search for a key that will likely collide and probe before returning -1
        assertEquals(-1, map.get(40));
    }
}
