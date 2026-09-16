package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void testPutPrimitives() {
        Universe universe = new Universe();
        int id = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, id);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);
        AttributeMap attrMap = sNode.attributes();

        attrMap.put("str", "value");
        assertEquals(AttributeValue.value("value"), attrMap.get("str"));

        attrMap.put("int", 123);
        assertEquals(AttributeValue.value(123), attrMap.get("int"));

        attrMap.put("long", 123L);
        assertEquals(AttributeValue.value(123L), attrMap.get("long"));

        attrMap.put("double", 1.23);
        assertEquals(AttributeValue.value(1.23), attrMap.get("double"));

        attrMap.put("boolean", true);
        assertEquals(AttributeValue.value(true), attrMap.get("boolean"));

        attrMap.put("bytearray", new byte[]{1, 2, 3});
        assertEquals(AttributeValue.value(new byte[]{1, 2, 3}), attrMap.get("bytearray"));
    }

    @Test
    public void testPutIfAbsentPrimitives() {
        Universe universe = new Universe();
        int id = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, id);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);
        AttributeMap attrMap = sNode.attributes();

        attrMap.putIfAbsent("str", "value");
        assertEquals(AttributeValue.value("value"), attrMap.get("str"));
        attrMap.putIfAbsent("str", "value2");
        assertEquals(AttributeValue.value("value"), attrMap.get("str"));

        attrMap.putIfAbsent("int", 123);
        assertEquals(AttributeValue.value(123), attrMap.get("int"));
        attrMap.putIfAbsent("int", 456);
        assertEquals(AttributeValue.value(123), attrMap.get("int"));

        attrMap.putIfAbsent("long", 123L);
        assertEquals(AttributeValue.value(123L), attrMap.get("long"));
        attrMap.putIfAbsent("long", 456L);
        assertEquals(AttributeValue.value(123L), attrMap.get("long"));

        attrMap.putIfAbsent("double", 1.23);
        assertEquals(AttributeValue.value(1.23), attrMap.get("double"));
        attrMap.putIfAbsent("double", 4.56);
        assertEquals(AttributeValue.value(1.23), attrMap.get("double"));

        attrMap.putIfAbsent("boolean", true);
        assertEquals(AttributeValue.value(true), attrMap.get("boolean"));
        attrMap.putIfAbsent("boolean", false);
        assertEquals(AttributeValue.value(true), attrMap.get("boolean"));

        attrMap.putIfAbsent("bytearray", new byte[]{1, 2, 3});
        assertEquals(AttributeValue.value(new byte[]{1, 2, 3}), attrMap.get("bytearray"));
        attrMap.putIfAbsent("bytearray", new byte[]{4, 5, 6});
        assertEquals(AttributeValue.value(new byte[]{1, 2, 3}), attrMap.get("bytearray"));
    }

    @Test
    public void testMergePrimitives() {
        Universe universe = new Universe();
        int id = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, id);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, uNode);
        AttributeMap attrMap = sNode.attributes();

        // String
        assertEquals(AttributeValue.value("val1"), attrMap.merge("str", "val1", (v1, v2) -> v2));
        assertEquals(AttributeValue.value("val2"), attrMap.merge("str", "val2", (v1, v2) -> v2));
        assertEquals(AttributeValue.value("val2"), attrMap.get("str"));

        // int
        assertEquals(AttributeValue.value(123), attrMap.merge("int", 123, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(456), attrMap.merge("int", 456, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(456), attrMap.get("int"));

        // long
        assertEquals(AttributeValue.value(123L), attrMap.merge("long", 123L, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(456L), attrMap.merge("long", 456L, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(456L), attrMap.get("long"));

        // double
        assertEquals(AttributeValue.value(1.23), attrMap.merge("double", 1.23, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(4.56), attrMap.merge("double", 4.56, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(4.56), attrMap.get("double"));

        // boolean
        assertEquals(AttributeValue.value(true), attrMap.merge("boolean", true, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(false), attrMap.merge("boolean", false, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(false), attrMap.get("boolean"));

        // byte[]
        assertEquals(AttributeValue.value(new byte[]{1}), attrMap.merge("bytearray", new byte[]{1}, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(new byte[]{2}), attrMap.merge("bytearray", new byte[]{2}, (v1, v2) -> v2));
        assertEquals(AttributeValue.value(new byte[]{2}), attrMap.get("bytearray"));
    }
}
