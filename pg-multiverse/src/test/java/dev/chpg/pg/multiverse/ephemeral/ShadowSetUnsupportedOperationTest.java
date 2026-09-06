package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import org.junit.jupiter.api.Test;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShadowSetUnsupportedOperationTest {

    @Test
    public void testShadowEdgeSetUnsupported() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges());

        Edge e = new EphemeralEdge(universe, -1, new EphemeralNode(universe, -1), new EphemeralNode(universe, -2));

        assertThrows(UnsupportedOperationException.class, () -> set.add(e));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(e));
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singleton(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singleton(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singleton(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
    }

    @Test
    public void testShadowNodeSetUnsupported() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes());

        Node n = new UniverseNode(universe, 1);

        assertThrows(UnsupportedOperationException.class, () -> set.add(n));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(n));
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singleton(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singleton(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singleton(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
    }
}
