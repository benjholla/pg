package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;

public class HeavyImmutableNodeSetTest {
    @Test
    public void testUnsupportedOperations() {
        HeavyNodeSet internalSet = new HeavyNodeSet();
        HeavyImmutableNodeSet set = new HeavyImmutableNodeSet(internalSet);
        HeavyNode n = (HeavyNode) new HeavyGraph().createNode();

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
        HeavyNodeSet internalSet = new HeavyNodeSet();
        HeavyNode n1 = (HeavyNode) new HeavyGraph().createNode();
        internalSet.add(n1);
        HeavyImmutableNodeSet set = new HeavyImmutableNodeSet(internalSet);

        assertTrue(set.contains(n1));
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));
        assertEquals(n1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new HeavyNode[0]));
        assertNotNull(set.spliterator());
        assertNotNull(set.stream());
        assertNotNull(set.parallelStream());
        assertNotNull(set.toString());

        assertEquals(internalSet.hashCode(), set.hashCode());
        assertTrue(set.equals(internalSet));
        assertTrue(set.equals(set));
    }
}
