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

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNode;
import dev.chpg.pg.multiverse.ephemeral.EphemeralEdge;

public class UniverseImmutableEdgeSetTest {
    private static final Universe universe = new Universe();

    @Test
    public void testUnsupportedOperations() {
        EphemeralGraph eg = new EphemeralGraph(universe);
        EphemeralNode n1 = (EphemeralNode) eg.factory().createNode();
        EphemeralNode n2 = (EphemeralNode) eg.factory().createNode();
        EphemeralEdge ee1 = (EphemeralEdge) eg.factory().createEdge(n1, n2);
        universe.promote(eg);

        UniverseEdgeSet internalSet = new UniverseEdgeSet(universe, new BitSet());
        UniverseImmutableEdgeSet set = new UniverseImmutableEdgeSet(internalSet);

        EphemeralGraph eg2 = new EphemeralGraph(universe);
        EphemeralNode en1 = (EphemeralNode) eg2.factory().createNode();
        EphemeralNode en2 = (EphemeralNode) eg2.factory().createNode();
        Edge e = eg2.factory().createEdge(en1, en2);

        assertThrows(UnsupportedOperationException.class, () -> set.add(e));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(e));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeIf(x -> true));

        // Testing iterator remove
        UniverseEdge e1 = new UniverseEdge(universe, 1);
        internalSet.add(e1); // modify backing set to test iterator behavior

        assertThrows(UnsupportedOperationException.class, () -> {
            var it = set.iterator();
            it.next();
            it.remove();
        });
    }

    @Test
    public void testConstructorValidation() {
        assertThrows(NullPointerException.class, () -> new UniverseImmutableEdgeSet(null));

        dev.chpg.pg.api.EdgeSet genericSet = new dev.chpg.pg.api.GenericImmutableEdgeSet(Collections.emptySet());
        assertThrows(IllegalArgumentException.class, () -> new UniverseImmutableEdgeSet(genericSet));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testDelegatedMethods() {
        UniverseEdgeSet internalSet = new UniverseEdgeSet(universe, new BitSet());
        UniverseEdge e1 = new UniverseEdge(universe, 1);
        internalSet.add(e1);
        UniverseImmutableEdgeSet set = new UniverseImmutableEdgeSet(internalSet);

        assertTrue(set.contains(e1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(e1)));
        assertEquals(e1, set.iterator().next());
        assertTrue(set.isSizeKnown());
        assertSame(universe, set.universe());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new UniverseEdge[0]));
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
        UniverseEdgeSet set = new UniverseEdgeSet(universe, new BitSet());
        UniverseEdge e1 = new UniverseEdge(universe, 1);
        UniverseEdge e2 = new UniverseEdge(universe, 2);
        set.add(e1);
        set.add(e2);

        UniverseImmutableEdgeSet immutableSet = new UniverseImmutableEdgeSet(set);
        assertSame(immutableSet, immutableSet.toImmutable());
    }

    @Test
    public void testDelegatedMethodsExtended() {
        UniverseEdgeSet internalSet = new UniverseEdgeSet(universe, new BitSet());
        UniverseEdge e1 = new UniverseEdge(universe, 1);
        internalSet.add(e1);
        UniverseImmutableEdgeSet set = new UniverseImmutableEdgeSet(internalSet);

        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
        set.forEach(edge -> count.incrementAndGet());
        assertEquals(1, count.get());

        dev.chpg.pg.api.Edge[] arr = set.toArray(dev.chpg.pg.api.Edge[]::new);
        assertEquals(1, arr.length);
        assertEquals(e1, arr[0]);

        assertEquals(e1, set.one().get());

        java.util.Set<Integer> ids = set.ids();
        assertEquals(1, ids.size());
        assertTrue(ids.contains(e1.id()));

        int[] toIds = set.toIdArray();
        assertEquals(1, toIds.length);
        assertEquals(e1.id(), toIds[0]);

        assertSame(set, set.materialize());
        assertTrue(set.isMaterialized());
    }

    @Test
    public void testIterator() {
        UniverseEdgeSet internalSet = new UniverseEdgeSet(universe, new BitSet());
        UniverseEdge e1 = new UniverseEdge(universe, 1);
        internalSet.add(e1);
        UniverseImmutableEdgeSet set = new UniverseImmutableEdgeSet(internalSet);

        java.util.Iterator<dev.chpg.pg.api.Edge> it = set.iterator();
        assertTrue(it.hasNext());
        assertEquals(e1, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSetAlgebra() {
        UniverseEdgeSet set = new UniverseEdgeSet(universe, new BitSet());
        UniverseEdge e1 = new UniverseEdge(universe, 1);
        UniverseEdge e2 = new UniverseEdge(universe, 2);
        UniverseEdge e3 = new UniverseEdge(universe, 3);
        set.add(e1);
        set.add(e2);

        UniverseImmutableEdgeSet immutableSet = new UniverseImmutableEdgeSet(set);

        dev.chpg.pg.api.EdgeSet intersect = immutableSet.intersect(java.util.Collections.singletonList(e2));
        assertEquals(1, intersect.size());
        assertTrue(intersect.contains(e2));

        dev.chpg.pg.api.EdgeSet difference = immutableSet.difference(java.util.Collections.singletonList(e1));
        assertEquals(1, difference.size());
        assertTrue(difference.contains(e2));

        dev.chpg.pg.api.EdgeSet union = immutableSet.union(java.util.Collections.singletonList(e3));
        assertEquals(3, union.size());
        assertTrue(union.contains(e1));
        assertTrue(union.contains(e2));
        assertTrue(union.contains(e3));
    }
}
