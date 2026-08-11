package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.api.Edge;

public class ShadowEdgeTest {
    @Test
    public void testShadowEdge() {
        Universe universe = new Universe();

        EphemeralGraph graph = new EphemeralGraph(universe);

        EphemeralNode n1 = new EphemeralNode(universe, -1);
        graph.addNode(n1);
        EphemeralNode n2 = new EphemeralNode(universe, -2);
        graph.addNode(n2);

        EphemeralEdge e = new EphemeralEdge(universe, -1, n1, n2);
        graph.addEdge(e);

        universe.promote(graph); // This will put the edge in the universe

        UniverseEdge createdEdge = (UniverseEdge) universe.asGraph().edges().iterator().next();

        EphemeralGraph graph2 = new EphemeralGraph(universe);
        ShadowEdge sEdge = new ShadowEdge(graph2, createdEdge);

        assertEquals(createdEdge.id(), sEdge.id());
        assertEquals(universe, sEdge.universe());
        assertEquals(createdEdge.hashCode(), sEdge.hashCode());

        assertTrue(sEdge.equals(sEdge));
        assertFalse(sEdge.equals(null));
        assertFalse(sEdge.equals(new Object()));

        ShadowEdge sEdge2 = new ShadowEdge(graph2, createdEdge);
        assertTrue(sEdge.equals(sEdge2));
        assertTrue(sEdge.equals(createdEdge));

        System.out.println(sEdge.toString());
        assertEquals(graph2, sEdge.transaction());
        assertEquals(createdEdge, sEdge.backingEdge());
    }
}
