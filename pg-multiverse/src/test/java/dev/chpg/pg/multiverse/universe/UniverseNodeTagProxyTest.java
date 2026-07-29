package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UniverseNodeTagProxyTest {

    private Universe universe;
    private int nodeId;
    private UniverseNodeTagProxy tags;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        nodeId = universe.idGenerator().createNodeId();
        tags = new UniverseNodeTagProxy(universe, nodeId);
    }

    @Test
    public void testInitialization() {
        assertEquals(0, tags.size());
        assertSame(universe, tags.universe());
    }

    @Test
    public void testAddAndContains() {
        assertTrue(tags.add("Person"));
        assertTrue(tags.contains("Person"));
        assertEquals(1, tags.size());

        assertFalse(tags.add("Person")); // Duplicate should return false
        assertEquals(1, tags.size());
    }

    @Test
    public void testContainsWrongType() {
        assertFalse(tags.contains(123));
        assertFalse(tags.contains(null));
    }

    @Test
    public void testRemove() {
        tags.add("Person");
        tags.add("Employee");

        assertTrue(tags.remove("Person"));
        assertFalse(tags.contains("Person"));
        assertTrue(tags.contains("Employee"));
        assertEquals(1, tags.size());

        assertFalse(tags.remove("Person")); // Already removed
        assertFalse(tags.remove(123)); // Wrong type
    }

    @Test
    public void testClear() {
        tags.add("Person");
        tags.add("Employee");
        assertEquals(2, tags.size());

        tags.clear();
        assertEquals(0, tags.size());
        assertFalse(tags.contains("Person"));
        assertFalse(tags.contains("Employee"));
    }

    @Test
    public void testIterator() {
        tags.add("A");
        tags.add("B");
        tags.add("C");

        Iterator<String> it = tags.iterator();
        int count = 0;
        boolean hasA = false;
        boolean hasB = false;
        boolean hasC = false;

        while (it.hasNext()) {
            String tag = it.next();
            if (tag.equals("A")) hasA = true;
            if (tag.equals("B")) hasB = true;
            if (tag.equals("C")) hasC = true;
            count++;
        }

        assertEquals(3, count);
        assertTrue(hasA);
        assertTrue(hasB);
        assertTrue(hasC);
    }

    @Test
    public void testIteratorFailFast() {
        tags.add("A");
        tags.add("B");

        Iterator<String> it = tags.iterator();
        assertTrue(it.hasNext());

        tags.add("C"); // modify while iterating

        assertThrows(ConcurrentModificationException.class, it::hasNext);
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    public void testNullInputs() {
        assertThrows(NullPointerException.class, () -> tags.add(null));
        assertThrows(NullPointerException.class, () -> new UniverseNodeTagProxy(null, nodeId));
    }
}
