package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ImmutableEmptyEdgeSetTest {

    private final ImmutableEmptyEdgeSet emptySet = new ImmutableEmptyEdgeSet();

    @Test
    public void testBasicProperties() {
        assertEquals(0, emptySet.size());
        assertTrue(emptySet.isEmpty());
        assertFalse(emptySet.iterator().hasNext());
        assertFalse(emptySet.contains(new TestDummyEdge()));
        assertEquals(Optional.empty(), emptySet.one());
        assertTrue(emptySet.ids().isEmpty());
        assertArrayEquals(new int[0], emptySet.toIdArray());
        assertTrue(emptySet.isMaterialized());
        assertTrue(emptySet.isSizeKnown());
    }

    @Test
    public void testImmutability() {
        assertSame(emptySet, emptySet.materialize());
        assertSame(emptySet, emptySet.toImmutable());
        assertThrows(UnsupportedOperationException.class, () -> emptySet.add(new TestDummyEdge()));
    }

    @Test
    public void testIntersect() {
        assertThrows(NullPointerException.class, () -> emptySet.intersect(null));
        assertSame(emptySet, emptySet.intersect(Collections.emptyList()));
        assertSame(emptySet, emptySet.intersect(List.of(new TestDummyEdge())));
    }

    @Test
    public void testDifference() {
        assertThrows(NullPointerException.class, () -> emptySet.difference(null));
        assertSame(emptySet, emptySet.difference(Collections.emptyList()));
        assertSame(emptySet, emptySet.difference(List.of(new TestDummyEdge())));
    }

    @Test
    public void testUnion() {
        assertThrows(NullPointerException.class, () -> emptySet.union(null));
        assertSame(emptySet, emptySet.union(Collections.emptyList()));

        Edge dummy = new TestDummyEdge();
        List<Edge> collection = List.of(dummy);
        EdgeSet unionResult = emptySet.union(collection);

        assertEquals(1, unionResult.size());
        assertTrue(unionResult.contains(dummy));

        EdgeSet immutableSet = new GenericImmutableEdgeSet(collection);
        EdgeSet unionResult2 = emptySet.union(immutableSet);
        assertSame(immutableSet, unionResult2);
    }
}
