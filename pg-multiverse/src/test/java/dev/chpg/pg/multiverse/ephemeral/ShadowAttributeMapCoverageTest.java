package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.api.AttributeMap;
import org.junit.jupiter.api.BeforeEach;

public class ShadowAttributeMapCoverageTest {

    private ShadowAttributeMap mapNode;
    private ShadowAttributeMap mapEdge;
    private EphemeralGraph transaction;

    @BeforeEach
    public void setUp() {
        Universe universe = new Universe();
        int nodeId = universe.idGenerator().createNodeId();
        UniverseNode uNode = new UniverseNode(universe, nodeId);
        uNode.attributes().put("k1", 10);

        int edgeId = universe.idGenerator().createEdgeId();
        UniverseEdge uEdge = new UniverseEdge(universe, edgeId);
        uEdge.attributes().put("k1", 10);

        transaction = new EphemeralGraph(universe);
        mapNode = new ShadowAttributeMap(transaction, uNode);
        mapEdge = new ShadowAttributeMap(transaction, uEdge);
    }

    @Test
    public void testCompute() {
        assertEquals(20, ((AttributeValue.IntegerValue) mapNode.compute("k1", (k, v) -> AttributeValue.value(20))).value());
        assertEquals(20, ((AttributeValue.IntegerValue) mapNode.get("k1")).value());

        assertEquals(20, ((AttributeValue.IntegerValue) mapEdge.compute("k1", (k, v) -> AttributeValue.value(20))).value());
        assertEquals(20, ((AttributeValue.IntegerValue) mapEdge.get("k1")).value());
    }

    @Test
    public void testComputeIfAbsent() {
        assertEquals(10, ((AttributeValue.IntegerValue) mapNode.computeIfAbsent("k1", k -> AttributeValue.value(20))).value());
        assertEquals(20, ((AttributeValue.IntegerValue) mapNode.computeIfAbsent("k2", k -> AttributeValue.value(20))).value());

        assertEquals(10, ((AttributeValue.IntegerValue) mapEdge.computeIfAbsent("k1", k -> AttributeValue.value(20))).value());
        assertEquals(20, ((AttributeValue.IntegerValue) mapEdge.computeIfAbsent("k2", k -> AttributeValue.value(20))).value());
    }

    @Test
    public void testComputeIfPresent() {
        assertEquals(20, ((AttributeValue.IntegerValue) mapNode.computeIfPresent("k1", (k, v) -> AttributeValue.value(20))).value());
        assertNull(mapNode.computeIfPresent("k2", (k, v) -> AttributeValue.value(20)));

        assertEquals(20, ((AttributeValue.IntegerValue) mapEdge.computeIfPresent("k1", (k, v) -> AttributeValue.value(20))).value());
        assertNull(mapEdge.computeIfPresent("k2", (k, v) -> AttributeValue.value(20)));
    }

    @Test
    public void testMerge() {
        assertEquals(20, ((AttributeValue.IntegerValue) mapNode.merge("k1", AttributeValue.value(20), (oldV, newV) -> newV)).value());
        assertEquals(20, ((AttributeValue.IntegerValue) mapNode.merge("k2", AttributeValue.value(20), (oldV, newV) -> newV)).value());

        assertEquals(20, ((AttributeValue.IntegerValue) mapEdge.merge("k1", AttributeValue.value(20), (oldV, newV) -> newV)).value());
        assertEquals(20, ((AttributeValue.IntegerValue) mapEdge.merge("k2", AttributeValue.value(20), (oldV, newV) -> newV)).value());
    }
}
