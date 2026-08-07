package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class UniverseNodeSetTest {

    private Universe universe;
    private UniverseNode n1, n2, n3;
    private UniverseNodeSet nodeSet;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        n1 = new UniverseNode(universe, 1);
        n2 = new UniverseNode(universe, 2);
        n3 = new UniverseNode(universe, 3);

        BitSet initialBits = new BitSet();
        initialBits.set(1);
        initialBits.set(2);
        nodeSet = new UniverseNodeSet(universe, initialBits);
    }

    @Test
    public void testUniverseView() {
        assertEquals(universe, nodeSet.universe());
    }

    @Test
    public void testSize() {
        assertEquals(2, nodeSet.size());
        assertTrue(nodeSet.isMaterialized());
        assertTrue(nodeSet.isSizeKnown());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testContains() {
        assertTrue(nodeSet.contains(n1));
        assertTrue(nodeSet.contains(n2));
        assertFalse(nodeSet.contains(n3));

        assertFalse(nodeSet.contains(null));
        assertFalse(nodeSet.contains("Not a Node"));

        Universe otherUniverse = new Universe();
        UniverseNode foreignNode = new UniverseNode(otherUniverse, 1);
        assertFalse(nodeSet.contains(foreignNode));

        dev.chpg.pg.api.Node mockNode = new dev.chpg.pg.api.Node() {
            public int id() { return 1; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertFalse(nodeSet.contains(mockNode));
    }

    @Test
    public void testAdd() {
        assertFalse(nodeSet.contains(n3));
        assertTrue(nodeSet.add(n3));
        assertTrue(nodeSet.contains(n3));
        assertEquals(3, nodeSet.size());

        assertFalse(nodeSet.add(n3)); // already present
        assertEquals(3, nodeSet.size());

        Universe otherUniverse = new Universe();
        UniverseNode foreignNode = new UniverseNode(otherUniverse, 4);
        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> nodeSet.add(foreignNode));
        assertTrue(e1.getMessage().contains("different Universe instance"));

        dev.chpg.pg.api.Node mockNode = new dev.chpg.pg.api.Node() {
            public int id() { return 1; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> nodeSet.add(mockNode));
        assertTrue(e2.getMessage().contains("Must be a UniverseNode"));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testRemove() {
        assertTrue(nodeSet.remove(n1));
        assertFalse(nodeSet.contains(n1));
        assertEquals(1, nodeSet.size());

        assertFalse(nodeSet.remove(n1)); // already removed
        assertEquals(1, nodeSet.size());

        assertFalse(nodeSet.remove(n3));
        assertFalse(nodeSet.remove(null));
        assertFalse(nodeSet.remove("Not a Node"));

        Universe otherUniverse = new Universe();
        UniverseNode foreignNode = new UniverseNode(otherUniverse, 1);
        assertFalse(nodeSet.remove(foreignNode));
    }

    @Test
    public void testAddAll() {
        assertTrue(nodeSet.addAll(Arrays.asList(n3, n2))); // n3 is new, n2 is present
        assertEquals(3, nodeSet.size());
        assertTrue(nodeSet.contains(n3));

        assertFalse(nodeSet.addAll(Arrays.asList(n1, n2))); // now n1, n2, n3 present, adding n1, n2 won't modify
    }

    @Test
    public void testRemoveAll() {
        assertTrue(nodeSet.removeAll(Arrays.asList(n1, n3)));
        assertEquals(1, nodeSet.size());
        assertFalse(nodeSet.contains(n1));
        assertTrue(nodeSet.contains(n2));

        assertFalse(nodeSet.removeAll(Arrays.asList(n1, n3))); // already removed
    }

    @Test
    public void testRetainAll() {
        assertTrue(nodeSet.retainAll(Arrays.asList(n2, n3)));
        assertEquals(1, nodeSet.size());
        assertFalse(nodeSet.contains(n1));
        assertTrue(nodeSet.contains(n2));

        assertFalse(nodeSet.retainAll(Arrays.asList(n2))); // no change
    }

    @Test
    public void testClear() {
        nodeSet.clear();
        assertEquals(0, nodeSet.size());
        assertTrue(nodeSet.isEmpty());
        assertFalse(nodeSet.contains(n1));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testIds() {
        Set<Integer> ids = nodeSet.ids();
        assertEquals(2, ids.size());
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));
        assertFalse(ids.contains(3));
        assertFalse(ids.contains("Not an Integer"));
        assertFalse(ids.contains(null));

        int count = 0;
        for (int id : ids) {
            assertTrue(id == 1 || id == 2);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testIdsConcurrentModification() {
        Set<Integer> ids = nodeSet.ids();
        Iterator<Integer> it = ids.iterator();
        assertTrue(it.hasNext());
        universe.incrementModCount(); // simulate structural change in universe
        assertThrows(ConcurrentModificationException.class, () -> it.hasNext());
        assertThrows(ConcurrentModificationException.class, () -> it.next());

        Iterator<Integer> it2 = ids.iterator();
        assertThrows(UnsupportedOperationException.class, () -> it2.remove());
    }

    @Test
    public void testToIdArray() {
        int[] ids = nodeSet.toIdArray();
        assertEquals(2, ids.length);
        assertArrayEquals(new int[]{1, 2}, ids);

        UniverseNodeSet set2 = new UniverseNodeSet(universe, new BitSet());
        assertEquals(0, set2.toIdArray().length);
    }

    @Test
    public void testToArray() {
        Object[] arr = nodeSet.toArray();
        assertEquals(2, arr.length);
        assertTrue(Arrays.asList(arr).contains(n1));
        assertTrue(Arrays.asList(arr).contains(n2));
    }

    @Test
    public void testToArrayTyped() {
        Node[] arr = nodeSet.toArray(new Node[0]);
        assertEquals(2, arr.length);
        assertTrue(Arrays.asList(arr).contains(n1));
        assertTrue(Arrays.asList(arr).contains(n2));

        Node[] largerArr = new Node[3];
        Node[] res = nodeSet.toArray(largerArr);
        assertEquals(largerArr, res);
        assertTrue(res[0].equals(n1) || res[0].equals(n2));
        assertTrue(res[1].equals(n1) || res[1].equals(n2));
        assertEquals(null, res[2]);
    }

    @Test
    public void testIterator() {
        int count = 0;
        Iterator<Node> it = nodeSet.iterator();
        while (it.hasNext()) {
            Node n = it.next();
            assertTrue(n.equals(n1) || n.equals(n2));
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testIteratorConcurrentModification() {
        Iterator<Node> it = nodeSet.iterator();
        assertTrue(it.hasNext());
        universe.incrementModCount(); // simulate structural change
        assertThrows(ConcurrentModificationException.class, () -> it.hasNext());
        assertThrows(ConcurrentModificationException.class, () -> it.next());

        Iterator<Node> it2 = nodeSet.iterator();
        assertThrows(UnsupportedOperationException.class, () -> it2.remove());
    }

    @Test
    public void testStream() {
        long count = nodeSet.stream().filter(n -> n.equals(n1) || n.equals(n2)).count();
        assertEquals(2, count);
    }

    @Test
    public void testSetAlgebraIntersect() {
        BitSet otherBits = new BitSet();
        otherBits.set(2);
        otherBits.set(3);
        UniverseNodeSet otherSet = new UniverseNodeSet(universe, otherBits);

        NodeSet intersection = nodeSet.intersect(otherSet);
        assertEquals(1, intersection.size());
        assertTrue(intersection.contains(n2));

        NodeSet emptyIntersect = nodeSet.intersect(new UniverseNodeSet(universe, new BitSet()));
        assertEquals(0, emptyIntersect.size());

        NodeSet foreignIntersect = nodeSet.intersect(Arrays.asList(n1, n3)); // collection fallback
        assertEquals(1, foreignIntersect.size());
        assertTrue(foreignIntersect.contains(n1));
    }

    @Test
    public void testSetAlgebraDifference() {
        BitSet otherBits = new BitSet();
        otherBits.set(2);
        otherBits.set(3);
        UniverseNodeSet otherSet = new UniverseNodeSet(universe, otherBits);

        NodeSet difference = nodeSet.difference(otherSet);
        assertEquals(1, difference.size());
        assertTrue(difference.contains(n1));

        NodeSet emptyDifference = nodeSet.difference(nodeSet);
        assertEquals(0, emptyDifference.size());

        NodeSet foreignDifference = nodeSet.difference(Arrays.asList(n1, n3)); // collection fallback
        assertEquals(1, foreignDifference.size());
        assertTrue(foreignDifference.contains(n2));
    }

    @Test
    public void testSetAlgebraUnion() {
        BitSet otherBits = new BitSet();
        otherBits.set(2);
        otherBits.set(3);
        UniverseNodeSet otherSet = new UniverseNodeSet(universe, otherBits);

        NodeSet union = nodeSet.union(otherSet);
        assertEquals(3, union.size());
        assertTrue(union.contains(n1));
        assertTrue(union.contains(n2));
        assertTrue(union.contains(n3));

        NodeSet emptyUnion = nodeSet.union(new UniverseNodeSet(universe, new BitSet()));
        assertEquals(2, emptyUnion.size());

        NodeSet collectionUnion = nodeSet.union(Arrays.asList(n3)); // collection fallback
        assertEquals(3, collectionUnion.size());
        assertTrue(collectionUnion.contains(n3));

        Universe otherUniverse = new Universe();
        UniverseNode foreignNode = new UniverseNode(otherUniverse, 4);
        assertThrows(IllegalArgumentException.class, () -> nodeSet.union(Arrays.asList(foreignNode)));

        dev.chpg.pg.api.Node mockNode = new dev.chpg.pg.api.Node() {
            public int id() { return 1; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertThrows(IllegalArgumentException.class, () -> nodeSet.union(Arrays.asList(mockNode)));
    }
}
