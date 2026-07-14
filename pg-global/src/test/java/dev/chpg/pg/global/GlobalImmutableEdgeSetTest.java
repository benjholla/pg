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
    public void testToImmutable() {
        dev.chpg.pg.global.GlobalNode n1 = new dev.chpg.pg.global.GlobalNode();
        dev.chpg.pg.global.GlobalEdge e1 = new dev.chpg.pg.global.GlobalEdge(n1, n1);
        dev.chpg.pg.global.GlobalEdge e2 = new dev.chpg.pg.global.GlobalEdge(n1, n1);

        dev.chpg.pg.global.GlobalEdgeSet set = new dev.chpg.pg.global.GlobalEdgeSet(e1, e2);
        dev.chpg.pg.global.GlobalImmutableEdgeSet immutableSet = new dev.chpg.pg.global.GlobalImmutableEdgeSet(set);

        assertSame(immutableSet, immutableSet.toImmutable());
    }

    @Test
    public void testSetAlgebra() {
        dev.chpg.pg.global.GlobalNode n1 = new dev.chpg.pg.global.GlobalNode();
        dev.chpg.pg.global.GlobalEdge e1 = new dev.chpg.pg.global.GlobalEdge(n1, n1);
        dev.chpg.pg.global.GlobalEdge e2 = new dev.chpg.pg.global.GlobalEdge(n1, n1);
        dev.chpg.pg.global.GlobalEdge e3 = new dev.chpg.pg.global.GlobalEdge(n1, n1);

        dev.chpg.pg.global.GlobalEdgeSet set = new dev.chpg.pg.global.GlobalEdgeSet(e1, e2);
        dev.chpg.pg.global.GlobalImmutableEdgeSet immutableSet = new dev.chpg.pg.global.GlobalImmutableEdgeSet(set);

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
