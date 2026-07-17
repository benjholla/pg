package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

public class GlobalImmutableNodeSetTest {
    @Test
    public void testUnsupportedOperations() {
        GlobalNodeSet internalSet = new GlobalNodeSet();
        GlobalImmutableNodeSet set = new GlobalImmutableNodeSet(internalSet);
        GlobalNode n = new GlobalNode();

        assertThrows(UnsupportedOperationException.class, () -> set.add(n));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(n));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeIf(x -> true));

        internalSet.add(n);
        assertThrows(UnsupportedOperationException.class, () -> {
            var it = set.iterator();
            it.next();
            it.remove();
        });
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testDelegatedMethods() {
        GlobalNodeSet internalSet = new GlobalNodeSet();
        GlobalNode n1 = new GlobalNode();
        internalSet.add(n1);
        GlobalImmutableNodeSet set = new GlobalImmutableNodeSet(internalSet);

        assertTrue(set.contains(n1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));
        assertEquals(n1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new GlobalNode[0]));
        assertNotNull(set.spliterator());
        assertNotNull(set.stream());
        assertNotNull(set.parallelStream());
        assertNotNull(set.toString());

        assertEquals(internalSet.hashCode(), set.hashCode());
        assertTrue(set.equals(internalSet));
        assertTrue(set.equals(set));
    }

    @Test
    public void testToImmutable() {
        dev.chpg.pg.global.GlobalNode n1 = new dev.chpg.pg.global.GlobalNode();
        dev.chpg.pg.global.GlobalNode n2 = new dev.chpg.pg.global.GlobalNode();

        dev.chpg.pg.global.GlobalNodeSet set = new dev.chpg.pg.global.GlobalNodeSet(n1, n2);
        dev.chpg.pg.global.GlobalImmutableNodeSet immutableSet = new dev.chpg.pg.global.GlobalImmutableNodeSet(set);

        assertSame(immutableSet, immutableSet.toImmutable());
    }


    @Test
    public void testDelegatedMethodsExtended() {
        GlobalNodeSet internalSet = new GlobalNodeSet();
        GlobalNode n1 = new GlobalNode();
        internalSet.add(n1);
        GlobalImmutableNodeSet set = new GlobalImmutableNodeSet(internalSet);

        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
        set.forEach(node -> count.incrementAndGet());
        assertEquals(1, count.get());

        dev.chpg.pg.api.Node[] arr = set.toArray(dev.chpg.pg.api.Node[]::new);
        assertEquals(1, arr.length);
        assertEquals(n1, arr[0]);

        assertEquals(n1, set.one().get());

        java.util.Set<Integer> ids = set.ids();
        assertEquals(1, ids.size());
        assertTrue(ids.contains(n1.id()));

        int[] toIds = set.toIdArray();
        assertEquals(1, toIds.length);
        assertEquals(n1.id(), toIds[0]);

        assertSame(set, set.materialize());
        assertTrue(set.isMaterialized());
    }

    @Test
    public void testIterator() {
        GlobalNodeSet internalSet = new GlobalNodeSet();
        GlobalNode n1 = new GlobalNode();
        internalSet.add(n1);
        GlobalImmutableNodeSet set = new GlobalImmutableNodeSet(internalSet);

        java.util.Iterator<dev.chpg.pg.api.Node> it = set.iterator();
        assertTrue(it.hasNext());
        assertEquals(n1, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSetAlgebra() {
        dev.chpg.pg.global.GlobalNode n1 = new dev.chpg.pg.global.GlobalNode();
        dev.chpg.pg.global.GlobalNode n2 = new dev.chpg.pg.global.GlobalNode();
        dev.chpg.pg.global.GlobalNode n3 = new dev.chpg.pg.global.GlobalNode();

        dev.chpg.pg.global.GlobalNodeSet set = new dev.chpg.pg.global.GlobalNodeSet(n1, n2);
        dev.chpg.pg.global.GlobalImmutableNodeSet immutableSet = new dev.chpg.pg.global.GlobalImmutableNodeSet(set);

        dev.chpg.pg.api.NodeSet intersect = immutableSet.intersect(java.util.Collections.singletonList(n2));
        assertEquals(1, intersect.size());
        assertTrue(intersect.contains(n2));

        dev.chpg.pg.api.NodeSet difference = immutableSet.difference(java.util.Collections.singletonList(n1));
        assertEquals(1, difference.size());
        assertTrue(difference.contains(n2));

        dev.chpg.pg.api.NodeSet union = immutableSet.union(java.util.Collections.singletonList(n3));
        assertEquals(3, union.size());
        assertTrue(union.contains(n1));
        assertTrue(union.contains(n2));
        assertTrue(union.contains(n3));
    }
}
