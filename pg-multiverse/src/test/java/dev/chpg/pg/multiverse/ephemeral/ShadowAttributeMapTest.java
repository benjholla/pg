package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShadowAttributeMapTest {

    private Universe universe;
    private EphemeralGraph graph;
    private Node n1;
    private EphemeralFactory factory;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        graph = new EphemeralGraph(universe);
        factory = graph.factory();

        n1 = factory.createNode();
        graph.addNode(n1);

        n1.attributes().put("key1", "val1");
        n1.attributes().put("key2", "val2");
    }

    @Test
    public void testShadowAttributeMapMethods() {
        AttributeMap attrs = n1.attributes(); // returns ShadowAttributeMap internally when fetched

        assertEquals(2, attrs.size());
        assertTrue(attrs.containsKey("key1"));

        attrs.compute("key1", (k, v) -> AttributeValue.value("newVal1"));
        assertEquals("newVal1", ((AttributeValue.StringValue) attrs.get("key1")).value());

        attrs.computeIfAbsent("key3", k -> AttributeValue.value("val3"));
        assertEquals("val3", ((AttributeValue.StringValue) attrs.get("key3")).value());

        attrs.computeIfPresent("key2", (k, v) -> AttributeValue.value("newVal2"));
        assertEquals("newVal2", ((AttributeValue.StringValue) attrs.get("key2")).value());

        attrs.merge("key3", AttributeValue.value("val4"), (v1, v2) -> AttributeValue.value("merged"));
        assertEquals("merged", ((AttributeValue.StringValue) attrs.get("key3")).value());
    }
}
