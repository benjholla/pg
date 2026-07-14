package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;

public class GlobalImmutableEdgeSetTest {
    @Test
    public void testUnsupportedOperations() {
        GlobalEdgeSet internalSet = new GlobalEdgeSet();
        GlobalImmutableEdgeSet set = new GlobalImmutableEdgeSet(internalSet);
        GlobalEdge e = new GlobalEdge(new GlobalNode(), new GlobalNode());

        assertThrows(UnsupportedOperationException.class, () -> set.add(e));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(e));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeIf(x -> true));

        internalSet.add(e);
        assertThrows(UnsupportedOperationException.class, () -> {
            var it = set.iterator();
            it.next();
            it.remove();
        });
    }

    @Test
    public void testDelegatedMethods() {
        GlobalEdgeSet internalSet = new GlobalEdgeSet();
        GlobalNode n1 = new GlobalNode();
        GlobalNode n2 = new GlobalNode();
        GlobalEdge e1 = new GlobalEdge(n1, n2);
        internalSet.add(e1);
        GlobalImmutableEdgeSet set = new GlobalImmutableEdgeSet(internalSet);

        assertTrue(set.contains(e1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(e1)));
        assertEquals(e1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new GlobalEdge[0]));
        assertNotNull(set.spliterator());
        assertNotNull(set.stream());
        assertNotNull(set.parallelStream());
        assertNotNull(set.toString());

        assertEquals(internalSet.hashCode(), set.hashCode());
        assertTrue(set.equals(internalSet));
        assertTrue(set.equals(set));
    }

    @Test
    public void testSetTheoreticMethods() {
        GlobalEdgeSet internalSet = new GlobalEdgeSet();
        GlobalEdge e1 = new GlobalEdge(new GlobalNode(), new GlobalNode());
        GlobalEdge e2 = new GlobalEdge(new GlobalNode(), new GlobalNode());
        internalSet.add(e1);
        GlobalImmutableEdgeSet set = new GlobalImmutableEdgeSet(internalSet);

        // one
        assertTrue(set.one().isPresent());
        assertEquals(e1, set.one().get());

        // forEach
        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
        set.forEach(n -> count.incrementAndGet());
        assertEquals(1, count.get());

        // materialize and toImmutable
        assertTrue(set.isMaterialized());
        assertEquals(set, set.materialize());
        assertEquals(set, set.toImmutable());

        // ids and toIdArray
        assertTrue(set.ids().contains(e1.id()));
        assertEquals(1, set.toIdArray().length);
        assertEquals(e1.id(), set.toIdArray()[0]);

        // intersect
        dev.chpg.pg.api.EdgeSet intersected = set.intersect(java.util.Collections.singletonList(e1));
        assertEquals(1, intersected.size());
        assertTrue(intersected.contains(e1));

        // difference
        dev.chpg.pg.api.EdgeSet diff = set.difference(java.util.Collections.singletonList(e1));
        assertEquals(0, diff.size());

        // union
        dev.chpg.pg.api.EdgeSet union = set.union(java.util.Collections.singletonList(e2));
        assertEquals(2, union.size());
        assertTrue(union.contains(e1));
        assertTrue(union.contains(e2));

        // toArray generator
        dev.chpg.pg.api.Edge[] array = set.toArray(dev.chpg.pg.api.Edge[]::new);
        assertEquals(1, array.length);
        assertEquals(e1, array[0]);
    }

    @Test
    public void testIteratorRemove() {
        GlobalEdgeSet internalSet = new GlobalEdgeSet();
        internalSet.add(new GlobalEdge(new GlobalNode(), new GlobalNode()));
        GlobalImmutableEdgeSet set = new GlobalImmutableEdgeSet(internalSet);

        java.util.Iterator<dev.chpg.pg.api.Edge> iterator = set.iterator();
        assertTrue(iterator.hasNext());
        iterator.next();

        try {
            iterator.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}
