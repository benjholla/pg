package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import dev.chpg.pg.multiverse.universe.UniverseNode;

public class ShadowNodeTest {
    @Test
    public void testShadowNode() {
        Universe universe = new Universe();
        int uNodeId = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, uNodeId);
        universe.asGraph().addNode(uNode);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);

        assertEquals(uNode.id(), sNode.id());
        assertEquals(universe, sNode.universe());
        assertEquals(uNode.hashCode(), sNode.hashCode());

        assertTrue(sNode.equals(sNode));
        assertFalse(sNode.equals(null));
        assertFalse(sNode.equals(new Object()));

        ShadowNode sNode2 = new ShadowNode(graph, uNode);
        assertTrue(sNode.equals(sNode2));
        assertTrue(sNode.equals(uNode));

        UniverseNode uNode2 = new UniverseNode(universe, universe.idGenerator().createNodeId());
        ShadowNode sNode3 = new ShadowNode(graph, uNode2);
        assertFalse(sNode.equals(sNode3));

        System.out.println(sNode.toString());
        assertEquals(graph, sNode.transaction());
    }
}
