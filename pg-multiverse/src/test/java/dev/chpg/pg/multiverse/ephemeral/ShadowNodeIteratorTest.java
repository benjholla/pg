package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collections;
import dev.chpg.pg.multiverse.universe.UniverseNode;

public class ShadowNodeIteratorTest {
    @Test
    public void testShadowNodeIteratorRemove() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);

        UniverseNode uNode = new UniverseNode(universe, universe.idGenerator().createNodeId());
        universe.asGraph().addNode(uNode);

        ShadowNodeIterator iter = new ShadowNodeIterator(graph, Collections.<Node>singletonList(uNode).iterator());
        assertTrue(iter.hasNext());
        Node next = iter.next();
        assertTrue(next instanceof ShadowNode);
        assertEquals(uNode.id(), next.id());

        assertThrows(UnsupportedOperationException.class, () -> iter.remove());
    }
}
