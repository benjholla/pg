package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.api.AttributeMap;

public class ShadowAttributeMapPutIfAbsentTest {
    @Test
    public void testPutIfAbsent() {
        Universe universe = new Universe();
        int id = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, id);
        uNode.attributes().put("k1", 10);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);

        AttributeMap attrMap = sNode.attributes();

        // This should not overwrite since k1 exists
        AttributeValue result1 = attrMap.putIfAbsent("k1", AttributeValue.value(20));
        assertEquals(10, ((AttributeValue.IntegerValue)result1).value());
        assertEquals(10, ((AttributeValue.IntegerValue)attrMap.get("k1")).value());

        // This should add k2 since it doesn't exist
        AttributeValue result2 = attrMap.putIfAbsent("k2", AttributeValue.value(30));
        assertNull(result2);
        assertEquals(30, ((AttributeValue.IntegerValue)attrMap.get("k2")).value());
    }
}
