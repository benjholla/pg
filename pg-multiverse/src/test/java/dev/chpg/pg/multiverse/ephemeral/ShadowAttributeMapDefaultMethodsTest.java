package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.api.AttributeMap;

public class ShadowAttributeMapDefaultMethodsTest {
    @Test
    public void testComputeMethods() {
        Universe universe = new Universe();
        int id = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, id);
        uNode.attributes().put("k1", 10);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);
        AttributeMap attrMap = sNode.attributes();

        attrMap.compute("k1", (k, v) -> AttributeValue.value(((AttributeValue.IntegerValue)v).value() + 5));
        assertEquals(15, ((AttributeValue.IntegerValue)attrMap.get("k1")).value());

        attrMap.computeIfAbsent("k2", k -> AttributeValue.value(20));
        assertEquals(20, ((AttributeValue.IntegerValue)attrMap.get("k2")).value());

        attrMap.computeIfPresent("k2", (k, v) -> AttributeValue.value(((AttributeValue.IntegerValue)v).value() + 5));
        assertEquals(25, ((AttributeValue.IntegerValue)attrMap.get("k2")).value());

        attrMap.merge("k3", AttributeValue.value(30), (v1, v2) -> AttributeValue.value(((AttributeValue.IntegerValue)v1).value() + ((AttributeValue.IntegerValue)v2).value()));
        assertEquals(30, ((AttributeValue.IntegerValue)attrMap.get("k3")).value());

        attrMap.merge("k3", AttributeValue.value(10), (v1, v2) -> AttributeValue.value(((AttributeValue.IntegerValue)v1).value() + ((AttributeValue.IntegerValue)v2).value()));
        assertEquals(40, ((AttributeValue.IntegerValue)attrMap.get("k3")).value());
    }
}
