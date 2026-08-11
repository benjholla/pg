package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.api.Edge;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.Collections;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;

public class ShadowEdgeIteratorTest {
    @Test
    public void testShadowEdgeIteratorRemove() {
        Universe universe = new Universe();

        EphemeralGraph graph = new EphemeralGraph(universe);

        EphemeralNode n1 = new EphemeralNode(universe, -1);
        graph.addNode(n1);
        EphemeralNode n2 = new EphemeralNode(universe, -2);
        graph.addNode(n2);

        EphemeralEdge e = new EphemeralEdge(universe, -1, n1, n2);
        graph.addEdge(e);

        universe.promote(graph);

        UniverseEdge createdEdge = (UniverseEdge) universe.asGraph().edges().iterator().next();

        EphemeralGraph graph2 = new EphemeralGraph(universe);

        // Create a direct iterator
        ShadowEdgeIterator iter = new ShadowEdgeIterator(graph2, Collections.<Edge>singletonList(createdEdge).iterator());
        assertTrue(iter.hasNext());
        Edge next = iter.next();
        assertTrue(next instanceof ShadowEdge);
        assertEquals(createdEdge.id(), next.id());

        assertThrows(UnsupportedOperationException.class, () -> iter.remove());
    }
}
