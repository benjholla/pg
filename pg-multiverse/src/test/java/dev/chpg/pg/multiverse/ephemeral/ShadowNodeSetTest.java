package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShadowNodeSetTest {

    @Test
    public void testUnsupportedOperations() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);
        Node node = graph.factory().createNode();
        graph.addNode(node);

        ShadowNodeSet shadowSet = (ShadowNodeSet) graph.nodes();

        assertThrows(UnsupportedOperationException.class, () -> shadowSet.add(node));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.remove(node));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.addAll(Collections.singletonList(node)));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.removeAll(Collections.singletonList(node)));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.retainAll(Collections.singletonList(node)));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.removeIf(x -> true));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.clear());
    }
}
