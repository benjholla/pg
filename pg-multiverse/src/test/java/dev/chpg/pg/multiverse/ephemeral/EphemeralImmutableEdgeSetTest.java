package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;

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

    @SuppressWarnings("unlikely-arg-type")
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
    public void testToImmutable() {
        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph g = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        dev.chpg.pg.multiverse.ephemeral.EphemeralEdge e1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralEdge) g.factory().createEdge(n1, n1);
        dev.chpg.pg.multiverse.ephemeral.EphemeralEdge e2 = (dev.chpg.pg.multiverse.ephemeral.EphemeralEdge) g.factory().createEdge(n1, n1);

        dev.chpg.pg.multiverse.ephemeral.EphemeralEdgeSet set = new dev.chpg.pg.multiverse.ephemeral.EphemeralEdgeSet(e1, e2);
        dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableEdgeSet immutableSet = new dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableEdgeSet(set);

        assertSame(immutableSet, immutableSet.toImmutable());
    }

    @Test
    public void testSetAlgebra() {
        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph g = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        dev.chpg.pg.multiverse.ephemeral.EphemeralEdge e1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralEdge) g.factory().createEdge(n1, n1);
        dev.chpg.pg.multiverse.ephemeral.EphemeralEdge e2 = (dev.chpg.pg.multiverse.ephemeral.EphemeralEdge) g.factory().createEdge(n1, n1);
        dev.chpg.pg.multiverse.ephemeral.EphemeralEdge e3 = (dev.chpg.pg.multiverse.ephemeral.EphemeralEdge) g.factory().createEdge(n1, n1);

        dev.chpg.pg.multiverse.ephemeral.EphemeralEdgeSet set = new dev.chpg.pg.multiverse.ephemeral.EphemeralEdgeSet(e1, e2);
        dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableEdgeSet immutableSet = new dev.chpg.pg.multiverse.ephemeral.EphemeralImmutableEdgeSet(set);

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
