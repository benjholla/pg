package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;

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
    public void testSetTheoreticMethods() {
        EphemeralNodeSet internalSet = new EphemeralNodeSet();
        dev.chpg.pg.api.Node n1 = factory.createNode();
        dev.chpg.pg.api.Node n2 = factory.createNode();
        internalSet.add(n1);
        EphemeralImmutableNodeSet set = new EphemeralImmutableNodeSet(internalSet);

        // one
        assertTrue(set.one().isPresent());
        assertEquals(n1, set.one().get());

        // forEach
        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
        set.forEach(n -> count.incrementAndGet());
        assertEquals(1, count.get());

        // materialize and toImmutable
        assertTrue(set.isMaterialized());
        assertEquals(set, set.materialize());
        assertEquals(set, set.toImmutable());

        // ids and toIdArray
        assertTrue(set.ids().contains(n1.id()));
        assertEquals(1, set.toIdArray().length);
        assertEquals(n1.id(), set.toIdArray()[0]);

        // intersect
        dev.chpg.pg.api.NodeSet intersected = set.intersect(java.util.Collections.singletonList(n1));
        assertEquals(1, intersected.size());
        assertTrue(intersected.contains(n1));

        // difference
        dev.chpg.pg.api.NodeSet diff = set.difference(java.util.Collections.singletonList(n1));
        assertEquals(0, diff.size());

        // union
        dev.chpg.pg.api.NodeSet union = set.union(java.util.Collections.singletonList(n2));
        assertEquals(2, union.size());
        assertTrue(union.contains(n1));
        assertTrue(union.contains(n2));

        // toArray generator
        dev.chpg.pg.api.Node[] array = set.toArray(dev.chpg.pg.api.Node[]::new);
        assertEquals(1, array.length);
        assertEquals(n1, array[0]);
    }

    @Test
    public void testIteratorRemove() {
        EphemeralNodeSet internalSet = new EphemeralNodeSet();
        internalSet.add(factory.createNode());
        EphemeralImmutableNodeSet set = new EphemeralImmutableNodeSet(internalSet);

        java.util.Iterator<dev.chpg.pg.api.Node> iterator = set.iterator();
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
