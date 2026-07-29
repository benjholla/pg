package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.BitSet;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNode;

public class UniverseImmutableNodeSetTest {
    private static final Universe universe = new Universe();

    @Test
    public void testUnsupportedOperations() {
        EphemeralGraph eg = new EphemeralGraph(universe);
        EphemeralNode en1 = (EphemeralNode) eg.factory().createNode();
        universe.promote(eg);

        UniverseNodeSet internalSet = new UniverseNodeSet(universe, new BitSet());
        UniverseImmutableNodeSet set = new UniverseImmutableNodeSet(internalSet);

        EphemeralGraph eg2 = new EphemeralGraph(universe);
        Node n = eg2.factory().createNode();

        assertThrows(UnsupportedOperationException.class, () -> set.add(n));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(n));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeIf(x -> true));

        // Testing iterator remove
        UniverseNode n1 = new UniverseNode(universe, 1);
        internalSet.add(n1); // modify backing set to test iterator behavior

        assertThrows(UnsupportedOperationException.class, () -> {
            var it = set.iterator();
            it.next();
            it.remove();
        });
    }

    @Test
    public void testConstructorValidation() {
        assertThrows(NullPointerException.class, () -> new UniverseImmutableNodeSet(null));

        dev.chpg.pg.api.NodeSet genericSet = new dev.chpg.pg.api.GenericImmutableNodeSet(Collections.emptySet());
        assertThrows(IllegalArgumentException.class, () -> new UniverseImmutableNodeSet(genericSet));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testDelegatedMethods() {
        UniverseNodeSet internalSet = new UniverseNodeSet(universe, new BitSet());
        UniverseNode n1 = new UniverseNode(universe, 1);
        internalSet.add(n1);
        UniverseImmutableNodeSet set = new UniverseImmutableNodeSet(internalSet);

        assertTrue(set.contains(n1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));
        assertEquals(n1, set.iterator().next());
        assertTrue(set.isSizeKnown());
        assertSame(universe, set.universe());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new UniverseNode[0]));
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
        UniverseNodeSet set = new UniverseNodeSet(universe, new BitSet());
        UniverseNode n1 = new UniverseNode(universe, 1);
        UniverseNode n2 = new UniverseNode(universe, 2);
        set.add(n1);
        set.add(n2);

        UniverseImmutableNodeSet immutableSet = new UniverseImmutableNodeSet(set);
        assertSame(immutableSet, immutableSet.toImmutable());
    }

    @Test
    public void testDelegatedMethodsExtended() {
        UniverseNodeSet internalSet = new UniverseNodeSet(universe, new BitSet());
        UniverseNode n1 = new UniverseNode(universe, 1);
        internalSet.add(n1);
        UniverseImmutableNodeSet set = new UniverseImmutableNodeSet(internalSet);

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
        UniverseNodeSet internalSet = new UniverseNodeSet(universe, new BitSet());
        UniverseNode n1 = new UniverseNode(universe, 1);
        internalSet.add(n1);
        UniverseImmutableNodeSet set = new UniverseImmutableNodeSet(internalSet);

        java.util.Iterator<dev.chpg.pg.api.Node> it = set.iterator();
        assertTrue(it.hasNext());
        assertEquals(n1, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSetAlgebra() {
        UniverseNodeSet set = new UniverseNodeSet(universe, new BitSet());
        UniverseNode n1 = new UniverseNode(universe, 1);
        UniverseNode n2 = new UniverseNode(universe, 2);
        UniverseNode n3 = new UniverseNode(universe, 3);
        set.add(n1);
        set.add(n2);

        UniverseImmutableNodeSet immutableSet = new UniverseImmutableNodeSet(set);

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
