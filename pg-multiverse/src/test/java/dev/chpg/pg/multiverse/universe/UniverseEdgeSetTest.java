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

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

public class UniverseEdgeSetTest {

    private Universe universe;
    private UniverseEdge e1, e2, e3;
    private UniverseEdgeSet edgeSet;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        e1 = new UniverseEdge(universe, 1);
        e2 = new UniverseEdge(universe, 2);
        e3 = new UniverseEdge(universe, 3);

        BitSet initialBits = new BitSet();
        initialBits.set(1);
        initialBits.set(2);
        edgeSet = new UniverseEdgeSet(universe, initialBits);
    }

    @Test
    public void testUniverseView() {
        assertEquals(universe, edgeSet.universe());
    }

    @Test
    public void testSize() {
        assertEquals(2, edgeSet.size());
        assertTrue(edgeSet.isMaterialized());
        assertTrue(edgeSet.isSizeKnown());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testContains() {
        assertTrue(edgeSet.contains(e1));
        assertTrue(edgeSet.contains(e2));
        assertFalse(edgeSet.contains(e3));

        assertFalse(edgeSet.contains(null));
        assertFalse(edgeSet.contains("Not an Edge"));

        Universe otherUniverse = new Universe();
        UniverseEdge foreignEdge = new UniverseEdge(otherUniverse, 1);
        assertFalse(edgeSet.contains(foreignEdge));

        dev.chpg.pg.api.Edge mockEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 1; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            public dev.chpg.pg.api.Node from() { return null; }
            public dev.chpg.pg.api.Node to() { return null; }
        };
        assertFalse(edgeSet.contains(mockEdge));
    }

    @Test
    public void testAdd() {
        assertFalse(edgeSet.contains(e3));
        assertTrue(edgeSet.add(e3));
        assertTrue(edgeSet.contains(e3));
        assertEquals(3, edgeSet.size());

        assertFalse(edgeSet.add(e3)); // already present
        assertEquals(3, edgeSet.size());

        Universe otherUniverse = new Universe();
        UniverseEdge foreignEdge = new UniverseEdge(otherUniverse, 4);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> edgeSet.add(foreignEdge));
        assertTrue(ex1.getMessage().contains("different Universe instance"));

        dev.chpg.pg.api.Edge mockEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 1; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            public dev.chpg.pg.api.Node from() { return null; }
            public dev.chpg.pg.api.Node to() { return null; }
        };
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> edgeSet.add(mockEdge));
        assertTrue(ex2.getMessage().contains("Must be a UniverseEdge"));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testRemove() {
        assertTrue(edgeSet.remove(e1));
        assertFalse(edgeSet.contains(e1));
        assertEquals(1, edgeSet.size());

        assertFalse(edgeSet.remove(e1)); // already removed
        assertEquals(1, edgeSet.size());

        assertFalse(edgeSet.remove(e3));
        assertFalse(edgeSet.remove(null));
        assertFalse(edgeSet.remove("Not an Edge"));

        Universe otherUniverse = new Universe();
        UniverseEdge foreignEdge = new UniverseEdge(otherUniverse, 1);
        assertFalse(edgeSet.remove(foreignEdge));
    }

    @Test
    public void testAddAll() {
        assertTrue(edgeSet.addAll(Arrays.asList(e3, e2))); // e3 is new, e2 is present
        assertEquals(3, edgeSet.size());
        assertTrue(edgeSet.contains(e3));

        assertFalse(edgeSet.addAll(Arrays.asList(e1, e2))); // already present now
    }

    @Test
    public void testRemoveAll() {
        assertTrue(edgeSet.removeAll(Arrays.asList(e1, e3)));
        assertEquals(1, edgeSet.size());
        assertFalse(edgeSet.contains(e1));
        assertTrue(edgeSet.contains(e2));

        assertFalse(edgeSet.removeAll(Arrays.asList(e1, e3))); // already removed
    }

    @Test
    public void testRetainAll() {
        assertTrue(edgeSet.retainAll(Arrays.asList(e2, e3)));
        assertEquals(1, edgeSet.size());
        assertFalse(edgeSet.contains(e1));
        assertTrue(edgeSet.contains(e2));

        assertFalse(edgeSet.retainAll(Arrays.asList(e2))); // no change
    }

    @Test
    public void testClear() {
        edgeSet.clear();
        assertEquals(0, edgeSet.size());
        assertTrue(edgeSet.isEmpty());
        assertFalse(edgeSet.contains(e1));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testIds() {
        Set<Integer> ids = edgeSet.ids();
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
        Set<Integer> ids = edgeSet.ids();
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
        int[] ids = edgeSet.toIdArray();
        assertEquals(2, ids.length);
        assertArrayEquals(new int[]{1, 2}, ids);

        UniverseEdgeSet set2 = new UniverseEdgeSet(universe, new BitSet());
        assertEquals(0, set2.toIdArray().length);
    }

    @Test
    public void testToArray() {
        Object[] arr = edgeSet.toArray();
        assertEquals(2, arr.length);
        assertTrue(Arrays.asList(arr).contains(e1));
        assertTrue(Arrays.asList(arr).contains(e2));
    }

    @Test
    public void testToArrayTyped() {
        Edge[] arr = edgeSet.toArray(new Edge[0]);
        assertEquals(2, arr.length);
        assertTrue(Arrays.asList(arr).contains(e1));
        assertTrue(Arrays.asList(arr).contains(e2));

        Edge[] largerArr = new Edge[3];
        Edge[] res = edgeSet.toArray(largerArr);
        assertEquals(largerArr, res);
        assertTrue(res[0].equals(e1) || res[0].equals(e2));
        assertTrue(res[1].equals(e1) || res[1].equals(e2));
        assertEquals(null, res[2]);
    }

    @Test
    public void testIterator() {
        int count = 0;
        Iterator<Edge> it = edgeSet.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            assertTrue(e.equals(e1) || e.equals(e2));
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testIteratorConcurrentModification() {
        Iterator<Edge> it = edgeSet.iterator();
        assertTrue(it.hasNext());
        universe.incrementModCount(); // simulate structural change
        assertThrows(ConcurrentModificationException.class, () -> it.hasNext());
        assertThrows(ConcurrentModificationException.class, () -> it.next());

        Iterator<Edge> it2 = edgeSet.iterator();
        assertThrows(UnsupportedOperationException.class, () -> it2.remove());
    }

    @Test
    public void testStream() {
        long count = edgeSet.stream().filter(e -> e.equals(e1) || e.equals(e2)).count();
        assertEquals(2, count);
    }

    @Test
    public void testSetAlgebraIntersect() {
        BitSet otherBits = new BitSet();
        otherBits.set(2);
        otherBits.set(3);
        UniverseEdgeSet otherSet = new UniverseEdgeSet(universe, otherBits);

        EdgeSet intersection = edgeSet.intersect(otherSet);
        assertEquals(1, intersection.size());
        assertTrue(intersection.contains(e2));

        EdgeSet emptyIntersect = edgeSet.intersect(new UniverseEdgeSet(universe, new BitSet()));
        assertEquals(0, emptyIntersect.size());

        EdgeSet foreignIntersect = edgeSet.intersect(Arrays.asList(e1, e3)); // collection fallback
        assertEquals(1, foreignIntersect.size());
        assertTrue(foreignIntersect.contains(e1));
    }

    @Test
    public void testSetAlgebraDifference() {
        BitSet otherBits = new BitSet();
        otherBits.set(2);
        otherBits.set(3);
        UniverseEdgeSet otherSet = new UniverseEdgeSet(universe, otherBits);

        EdgeSet difference = edgeSet.difference(otherSet);
        assertEquals(1, difference.size());
        assertTrue(difference.contains(e1));

        EdgeSet emptyDifference = edgeSet.difference(edgeSet);
        assertEquals(0, emptyDifference.size());

        EdgeSet foreignDifference = edgeSet.difference(Arrays.asList(e1, e3)); // collection fallback
        assertEquals(1, foreignDifference.size());
        assertTrue(foreignDifference.contains(e2));
    }

    @Test
    public void testSetAlgebraUnion() {
        BitSet otherBits = new BitSet();
        otherBits.set(2);
        otherBits.set(3);
        UniverseEdgeSet otherSet = new UniverseEdgeSet(universe, otherBits);

        EdgeSet union = edgeSet.union(otherSet);
        assertEquals(3, union.size());
        assertTrue(union.contains(e1));
        assertTrue(union.contains(e2));
        assertTrue(union.contains(e3));

        EdgeSet emptyUnion = edgeSet.union(new UniverseEdgeSet(universe, new BitSet()));
        assertEquals(2, emptyUnion.size());

        EdgeSet collectionUnion = edgeSet.union(Arrays.asList(e3)); // collection fallback
        assertEquals(3, collectionUnion.size());
        assertTrue(collectionUnion.contains(e3));

        Universe otherUniverse = new Universe();
        UniverseEdge foreignEdge = new UniverseEdge(otherUniverse, 4);
        assertThrows(IllegalArgumentException.class, () -> edgeSet.union(Arrays.asList(foreignEdge)));

        dev.chpg.pg.api.Edge mockEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 1; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            public dev.chpg.pg.api.Node from() { return null; }
            public dev.chpg.pg.api.Node to() { return null; }
        };
        assertThrows(IllegalArgumentException.class, () -> edgeSet.union(Arrays.asList(mockEdge)));
    }
}
