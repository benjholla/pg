package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.TagSet;

public class TagSetTest {

    private TagSet tagSet;

    @BeforeEach
    public void setUp() {
        tagSet = new GlobalTagSet();
    }

    @Test
    public void testAddAndSize() {
        assertTrue(tagSet.isEmpty());
        assertEquals(0, tagSet.size());

        assertTrue(tagSet.add("tag1"));
        assertFalse(tagSet.add("tag1")); // Duplicate add should return false

        assertFalse(tagSet.isEmpty());
        assertEquals(1, tagSet.size());

        assertTrue(tagSet.add("tag2"));
        assertEquals(2, tagSet.size());
    }

    @Test
    public void testContains() {
        tagSet.add("tag1");
        assertTrue(tagSet.contains("tag1"));
        assertFalse(tagSet.contains("tag2"));
    }

    @Test
    public void testRemove() {
        tagSet.add("tag1");
        tagSet.add("tag2");

        assertTrue(tagSet.remove("tag1"));
        assertFalse(tagSet.contains("tag1"));
        assertEquals(1, tagSet.size());

        assertFalse(tagSet.remove("tag3"));
    }

    @Test
    public void testClear() {
        tagSet.add("tag1");
        tagSet.add("tag2");
        tagSet.clear();
        assertTrue(tagSet.isEmpty());
        assertEquals(0, tagSet.size());
    }

    @Test
    public void testAddAll() {
        List<String> tags = Arrays.asList("tag1", "tag2", "tag3");
        assertTrue(tagSet.addAll(tags));
        assertEquals(3, tagSet.size());
        assertTrue(tagSet.containsAll(tags));
    }

    @Test
    public void testRemoveAll() {
        tagSet.addAll(Arrays.asList("tag1", "tag2", "tag3", "tag4"));
        assertTrue(tagSet.removeAll(Arrays.asList("tag2", "tag4")));
        assertEquals(2, tagSet.size());
        assertTrue(tagSet.contains("tag1"));
        assertTrue(tagSet.contains("tag3"));
    }

    @Test
    public void testRetainAll() {
        tagSet.addAll(Arrays.asList("tag1", "tag2", "tag3", "tag4"));
        assertTrue(tagSet.retainAll(Arrays.asList("tag2", "tag4", "tag5")));
        assertEquals(2, tagSet.size());
        assertTrue(tagSet.contains("tag2"));
        assertTrue(tagSet.contains("tag4"));
        assertFalse(tagSet.contains("tag1"));
    }

    @Test
    public void testIterator() {
        tagSet.addAll(Arrays.asList("tag1", "tag2"));
        Iterator<String> it = tagSet.iterator();
        assertTrue(it.hasNext());
        String first = it.next();
        assertTrue(first.equals("tag1") || first.equals("tag2"));
        assertTrue(it.hasNext());
        String second = it.next();
        assertTrue(second.equals("tag1") || second.equals("tag2"));
        assertNotEquals(first, second);
        assertFalse(it.hasNext());
    }

    @Test
    public void testToArray() {
        tagSet.addAll(Arrays.asList("tag1", "tag2"));
        Object[] arr = tagSet.toArray();
        assertEquals(2, arr.length);
        List<Object> list = Arrays.asList(arr);
        assertTrue(list.contains("tag1"));
        assertTrue(list.contains("tag2"));
    }

    @Test
    public void testToArrayWithType() {
        tagSet.addAll(Arrays.asList("tag1", "tag2"));
        String[] arr = tagSet.toArray(new String[0]);
        assertEquals(2, arr.length);
        List<String> list = Arrays.asList(arr);
        assertTrue(list.contains("tag1"));
        assertTrue(list.contains("tag2"));
    }

    @Test
    public void testEqualsAndHashCode() {
        TagSet tagSet2 = new GlobalTagSet();
        assertEquals(tagSet, tagSet2);
        assertEquals(tagSet.hashCode(), tagSet2.hashCode());

        tagSet.add("tag1");
        assertNotEquals(tagSet, tagSet2);

        tagSet2.add("tag1");
        assertEquals(tagSet, tagSet2);
        assertEquals(tagSet.hashCode(), tagSet2.hashCode());
    }

    @Test
    public void testToString() {
        tagSet.add("tag1");
        String str = tagSet.toString();
        assertTrue(str.startsWith("["));
        assertTrue(str.contains("tag1"));
        assertTrue(str.endsWith("]"));
    }


    @Test
    public void testCollectionConstructor() {
        java.util.List<String> tags = java.util.Arrays.asList("tag1", "tag2");
        GlobalTagSet tagSet = new GlobalTagSet(tags);
        assertEquals(2, tagSet.size());
        assertTrue(tagSet.contains("tag1"));
        assertTrue(tagSet.contains("tag2"));
    }
}
