package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.api.AttributeMap;

public class ShadowAttributeMapClearTest {
    @Test
    public void testClear() {
        Universe universe = new Universe();
        int id = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, id);
        uNode.attributes().put("k1", 10);
        uNode.attributes().put("k2", 20);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);

        // Before clear
        assertEquals(10, ((AttributeValue.IntegerValue)sNode.attributes().get("k1")).value());

        // This is the important part: explicitly calling clear on the AttributeMap
        AttributeMap attrMap = sNode.attributes();
        attrMap.clear();

        assertNull(sNode.attributes().get("k1"));
        assertNull(sNode.attributes().get("k2"));
        assertEquals(0, sNode.attributes().size());
    }
}
