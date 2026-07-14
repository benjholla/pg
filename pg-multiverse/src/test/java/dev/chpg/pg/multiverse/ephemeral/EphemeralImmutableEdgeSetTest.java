package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;

import dev.chpg.pg.api.Node;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;

public class EphemeralImmutableEdgeSetTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    @Test
    public void testUnsupportedOperations() {
        EphemeralEdgeSet internalSet = new EphemeralEdgeSet();
        EphemeralImmutableEdgeSet set = new EphemeralImmutableEdgeSet(internalSet);
        Edge e = factory.createEdge(factory.createNode(), factory.createNode());

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
        EphemeralEdgeSet internalSet = new EphemeralEdgeSet();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);
        internalSet.add(e1);
        EphemeralImmutableEdgeSet set = new EphemeralImmutableEdgeSet(internalSet);

        assertTrue(set.contains(e1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(e1)));
        assertEquals(e1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new EphemeralEdge[0]));
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
        EphemeralEdgeSet internalSet = new EphemeralEdgeSet();
        dev.chpg.pg.api.Edge e1 = factory.createEdge(factory.createNode(), factory.createNode());
        dev.chpg.pg.api.Edge e2 = factory.createEdge(factory.createNode(), factory.createNode());
        internalSet.add(e1);
        EphemeralImmutableEdgeSet set = new EphemeralImmutableEdgeSet(internalSet);

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
        EphemeralEdgeSet internalSet = new EphemeralEdgeSet();
        internalSet.add(factory.createEdge(factory.createNode(), factory.createNode()));
        EphemeralImmutableEdgeSet set = new EphemeralImmutableEdgeSet(internalSet);

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
