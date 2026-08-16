package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.api.AttributeValue;

public class ShadowAttributeMapTest {
    @Test
    public void testShadowAttributeMapNode() {
        Universe universe = new Universe();
        int nId = universe.idGenerator().createNodeId();
        UniverseNode n = new UniverseNode(universe, nId);
        universe.asGraph().addNode(n);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, n);

        assertNull(sNode.attributes().put("key1", AttributeValue.value(10)));
        assertEquals(10, ((AttributeValue.IntegerValue)sNode.attributes().get("key1")).value());

        AttributeValue prev = sNode.attributes().remove("key1");
        assertEquals(10, ((AttributeValue.IntegerValue)prev).value());
        assertNull(sNode.attributes().get("key1"));

        assertNull(sNode.attributes().get(123)); // object test
        assertNull(sNode.attributes().remove(123)); // object test
        assertFalse(sNode.attributes().containsKey(123)); // object test

        n.attributes().put("universe_key", AttributeValue.value(20));
        assertEquals(20, ((AttributeValue.IntegerValue)sNode.attributes().get("universe_key")).value());
        assertTrue(sNode.attributes().containsKey("universe_key"));

        AttributeValue prev2 = sNode.attributes().remove("universe_key");
        assertEquals(20, ((AttributeValue.IntegerValue)prev2).value());
        assertNull(sNode.attributes().get("universe_key"));
        assertFalse(sNode.attributes().containsKey("universe_key"));
        assertEquals(20, ((AttributeValue.IntegerValue)n.attributes().get("universe_key")).value()); // Still in universe

        // entrySet iterator and size
        sNode.attributes().put("key2", AttributeValue.value(30));
        sNode.attributes().put("key3", AttributeValue.value(40));

        int count = 0;
        for (java.util.Map.Entry<String, AttributeValue> entry : sNode.attributes().entrySet()) {
            count++;
        }
        assertEquals(2, count);

        // 1 missed instruction test: check missing key coverage on containsKey
        assertFalse(sNode.attributes().containsKey("non_existent"));
    }

    @Test
    public void testShadowAttributeMapEdge() {
        Universe universe = new Universe();

        EphemeralGraph graph = new EphemeralGraph(universe);

        EphemeralNode n1 = new EphemeralNode(universe, -1);
        graph.addNode(n1);
        EphemeralNode n2 = new EphemeralNode(universe, -2);
        graph.addNode(n2);

        EphemeralEdge e = new EphemeralEdge(universe, -1, n1, n2);
        graph.addEdge(e);

        universe.promote(graph);

        UniverseEdge createdEdge = (UniverseEdge) universe.asGraph().edges().iterator().next();

        EphemeralGraph graph2 = new EphemeralGraph(universe);
        ShadowEdge sEdge = new ShadowEdge(graph2, createdEdge);

        assertNull(sEdge.attributes().put("key1", AttributeValue.value(10)));
        assertEquals(10, ((AttributeValue.IntegerValue)sEdge.attributes().get("key1")).value());

        AttributeValue prev = sEdge.attributes().remove("key1");
        assertEquals(10, ((AttributeValue.IntegerValue)prev).value());
        assertNull(sEdge.attributes().get("key1"));

        assertNull(sEdge.attributes().get(123)); // object test
        assertNull(sEdge.attributes().remove(123)); // object test
        assertFalse(sEdge.attributes().containsKey(123)); // object test

        createdEdge.attributes().put("universe_key", AttributeValue.value(20));
        assertEquals(20, ((AttributeValue.IntegerValue)sEdge.attributes().get("universe_key")).value());
        assertTrue(sEdge.attributes().containsKey("universe_key"));

        AttributeValue prev2 = sEdge.attributes().remove("universe_key");
        assertEquals(20, ((AttributeValue.IntegerValue)prev2).value());
        assertNull(sEdge.attributes().get("universe_key"));
        assertFalse(sEdge.attributes().containsKey("universe_key"));
        assertEquals(20, ((AttributeValue.IntegerValue)createdEdge.attributes().get("universe_key")).value()); // Still in universe

        // entrySet iterator and size
        sEdge.attributes().put("key2", AttributeValue.value(30));
        sEdge.attributes().put("key3", AttributeValue.value(40));

        int count = 0;
        for (java.util.Map.Entry<String, AttributeValue> entry : sEdge.attributes().entrySet()) {
            count++;
        }
        assertEquals(2, count);
    }
}
