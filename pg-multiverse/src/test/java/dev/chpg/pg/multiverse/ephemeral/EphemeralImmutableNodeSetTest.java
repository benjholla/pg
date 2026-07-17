package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;

public class EphemeralImmutableNodeSetTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    @Test
    public void testUnsupportedOperations() {
        EphemeralNodeSet internalSet = new EphemeralNodeSet();
        EphemeralImmutableNodeSet set = new EphemeralImmutableNodeSet(internalSet);
        Node n = factory.createNode();

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
        EphemeralNodeSet internalSet = new EphemeralNodeSet();
        Node n1 = factory.createNode();
        internalSet.add(n1);
        EphemeralImmutableNodeSet set = new EphemeralImmutableNodeSet(internalSet);

        assertTrue(set.contains(n1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));
        assertEquals(n1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new EphemeralNode[0]));
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
        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph g = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n2 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();

        dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet set = new dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet(n1, n2);
        dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet immutableSet = new dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet(set);

        assertSame(immutableSet, immutableSet.toImmutable());
    }


    @Test
    public void testDelegatedMethodsExtended() {
        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph g = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet internalSet = new dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        internalSet.add(n1);
        dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet set = new dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet(internalSet);

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
        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph g = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet internalSet = new dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        internalSet.add(n1);
        dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet set = new dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet(internalSet);

        java.util.Iterator<dev.chpg.pg.api.Node> it = set.iterator();
        assertTrue(it.hasNext());
        assertEquals(n1, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSetAlgebra() {
        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph g = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n2 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n3 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();

        dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet set = new dev.chpg.pg.multiverse.ephemeral.EphemeralNodeSet(n1, n2);
        dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet immutableSet = new dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableNodeSet(set);

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
