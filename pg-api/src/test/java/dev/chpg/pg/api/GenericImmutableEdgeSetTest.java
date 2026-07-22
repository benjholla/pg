package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GenericImmutableEdgeSetTest {

    @Test
    public void testBasicProperties() {
        Edge e1 = new TestDummyEdge() { @Override public int id() { return 1; } };
        Edge e2 = new TestDummyEdge() { @Override public int id() { return 2; } };

        GenericImmutableEdgeSet set = new GenericImmutableEdgeSet(List.of(e1, e2));

        assertEquals(2, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.contains(e1));
        assertTrue(set.contains(e2));
        assertFalse(set.contains(new TestDummyEdge() { @Override public int id() { return 3; } }));

        assertTrue(set.one().isPresent());
        assertTrue(set.one().get() == e1 || set.one().get() == e2);

        assertTrue(set.ids().contains(1));
        assertTrue(set.ids().contains(2));

        int[] arr = set.toIdArray();
        assertEquals(2, arr.length);
        assertTrue(arr[0] == 1 || arr[0] == 2);
        assertTrue(arr[1] == 1 || arr[1] == 2);

        assertTrue(set.isMaterialized());
        assertTrue(set.isSizeKnown());
    }

    @Test
    public void testEmptyProperties() {
        GenericImmutableEdgeSet emptySet = new GenericImmutableEdgeSet(Collections.emptyList());
        assertEquals(0, emptySet.size());
        assertTrue(emptySet.isEmpty());
        assertFalse(emptySet.one().isPresent());
        assertTrue(emptySet.ids().isEmpty());
        assertEquals(0, emptySet.toIdArray().length);
    }

    @Test
    public void testImmutability() {
        GenericImmutableEdgeSet set = new GenericImmutableEdgeSet(List.of(new TestDummyEdge()));
        assertSame(set, set.materialize());
        assertSame(set, set.toImmutable());
        assertThrows(UnsupportedOperationException.class, () -> set.add(new TestDummyEdge()));
    }

    @Test
    public void testIntersect() {
        Edge e1 = new TestDummyEdge() { @Override public int id() { return 1; } };
        Edge e2 = new TestDummyEdge() { @Override public int id() { return 2; } };
        GenericImmutableEdgeSet set = new GenericImmutableEdgeSet(List.of(e1, e2));

        assertThrows(NullPointerException.class, () -> set.intersect(null));

        assertTrue(set.intersect(Collections.emptyList()).isEmpty());

        EdgeSet intersection = set.intersect(List.of(e1, new TestDummyEdge() { @Override public int id() { return 3; } }));
        assertEquals(1, intersection.size());
        assertTrue(intersection.contains(e1));
    }

    @Test
    public void testDifference() {
        Edge e1 = new TestDummyEdge() { @Override public int id() { return 1; } };
        Edge e2 = new TestDummyEdge() { @Override public int id() { return 2; } };
        GenericImmutableEdgeSet set = new GenericImmutableEdgeSet(List.of(e1, e2));

        assertThrows(NullPointerException.class, () -> set.difference(null));

        assertSame(set, set.difference(Collections.emptyList()));

        EdgeSet diff = set.difference(List.of(e1, new TestDummyEdge() { @Override public int id() { return 3; } }));
        assertEquals(1, diff.size());
        assertTrue(diff.contains(e2));

        EdgeSet emptyDiff = set.difference(List.of(e1, e2));
        assertTrue(emptyDiff.isEmpty());
    }

    @Test
    public void testUnion() {
        Edge e1 = new TestDummyEdge() { @Override public int id() { return 1; } };
        Edge e2 = new TestDummyEdge() { @Override public int id() { return 2; } };
        GenericImmutableEdgeSet set = new GenericImmutableEdgeSet(List.of(e1));

        assertThrows(NullPointerException.class, () -> set.union(null));

        assertSame(set, set.union(Collections.emptyList()));

        EdgeSet union = set.union(List.of(e2));
        assertEquals(2, union.size());
        assertTrue(union.contains(e1));
        assertTrue(union.contains(e2));
    }
}
