package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShadowEdgeSetTest {

    @Test
    public void testUnsupportedOperations() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);
        Node from = graph.factory().createNode();
        Node to = graph.factory().createNode();
        Edge edge = graph.factory().createEdge(from, to);
        graph.addEdge(edge);

        ShadowEdgeSet shadowSet = (ShadowEdgeSet) graph.edges();

        assertThrows(UnsupportedOperationException.class, () -> shadowSet.add(edge));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.remove(edge));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.addAll(Collections.singletonList(edge)));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.removeAll(Collections.singletonList(edge)));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.retainAll(Collections.singletonList(edge)));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.removeIf(x -> true));
        assertThrows(UnsupportedOperationException.class, () -> shadowSet.clear());
    }
}
