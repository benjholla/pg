package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;

public class HeavyImmutableEdgeSetTest {
    @Test
    public void testUnsupportedOperations() {
        HeavyEdgeSet internalSet = new HeavyEdgeSet();
        HeavyImmutableEdgeSet set = new HeavyImmutableEdgeSet(internalSet);
        HeavyEdge e = new HeavyEdge(new HeavyNode(), new HeavyNode());

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
        HeavyEdgeSet internalSet = new HeavyEdgeSet();
        HeavyNode n1 = new HeavyNode();
        HeavyNode n2 = new HeavyNode();
        HeavyEdge e1 = new HeavyEdge(n1, n2);
        internalSet.add(e1);
        HeavyImmutableEdgeSet set = new HeavyImmutableEdgeSet(internalSet);

        assertTrue(set.contains(e1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(e1)));
        assertEquals(e1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new HeavyEdge[0]));
        assertNotNull(set.spliterator());
        assertNotNull(set.stream());
        assertNotNull(set.parallelStream());
        assertNotNull(set.toString());

        assertEquals(internalSet.hashCode(), set.hashCode());
        assertTrue(set.equals(internalSet));
        assertTrue(set.equals(set));
    }
}
