package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.api.AttributeMap;

public class ShadowAttributeMapReplaceAllTest {
    @Test
    public void testReplaceAll() {
        Universe universe = new Universe();
        int id = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, id);
        uNode.attributes().put("k1", 10);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);

        // This is the important part: explicitly calling replaceAll on the AttributeMap
        AttributeMap attrMap = sNode.attributes();
        attrMap.replaceAll((k, v) -> AttributeValue.value(((AttributeValue.IntegerValue)v).value() * 2));

        assertEquals(20, ((AttributeValue.IntegerValue)sNode.attributes().get("k1")).value());
    }
}
