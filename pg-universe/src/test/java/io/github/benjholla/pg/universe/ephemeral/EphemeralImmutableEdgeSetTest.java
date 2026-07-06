package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;

public class EphemeralImmutableEdgeSetTest {
    private static final EphemeralGraph factory = new EphemeralGraph();

    @Test
    public void testUnsupportedOperations() {
        EphemeralEdgeSet internalSet = new EphemeralEdgeSet();
        EphemeralImmutableEdgeSet set = new EphemeralImmutableEdgeSet(internalSet);
        EphemeralEdge e = factory.createEdge(factory.createNode(), factory.createNode());

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
        EphemeralNode n1 = factory.createNode();
        EphemeralNode n2 = factory.createNode();
        EphemeralEdge e1 = factory.createEdge(n1, n2);
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
}
