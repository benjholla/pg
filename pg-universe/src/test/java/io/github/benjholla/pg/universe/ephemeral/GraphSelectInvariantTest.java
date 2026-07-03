package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

/**
 * Validates the behavior of selectNodes and selectEdges operations.
 */
public class GraphSelectInvariantTest {

    private EphemeralGraph graph;
    private Node a, b, c;
    private Edge ab, bc, ca;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();

        a = new EphemeralNode();
        a.attributes().put("color", new AttributeValue.StringVal("red"));
        a.attributes().put("weight", new AttributeValue.IntVal(10));

        b = new EphemeralNode();
        b.attributes().put("color", new AttributeValue.StringVal("blue"));

        c = new EphemeralNode();
        c.attributes().put("weight", new AttributeValue.IntVal(20));

        ab = new EphemeralEdge(a, b);
        ab.attributes().put("type", new AttributeValue.StringVal("friend"));

        bc = new EphemeralEdge(b, c);
        bc.attributes().put("type", new AttributeValue.StringVal("enemy"));

        ca = new EphemeralEdge(c, a);
        ca.attributes().put("distance", new AttributeValue.IntVal(50));

        graph.add(a);
        graph.add(b);
        graph.add(c);
        graph.add(ab);
        graph.add(bc);
        graph.add(ca);
    }

    @Test
    public void testSelectNodesByAttributeKeyOnly() {
        NodeSet coloredNodes = graph.selectNodes("color");
        assertEquals(2, coloredNodes.size(), "Should find all nodes with a 'color' attribute");
        assertTrue(coloredNodes.contains(a));
        assertTrue(coloredNodes.contains(b));
        assertFalse(coloredNodes.contains(c));
    }

    @Test
    public void testSelectNodesByAttributeKeyAndValue() {
        NodeSet redNodes = graph.selectNodes("color", new AttributeValue.StringVal("red"));
        assertEquals(1, redNodes.size(), "Should find only nodes with color='red'");
        assertTrue(redNodes.contains(a));

        // Test multiple values
        NodeSet selectedNodes = graph.selectNodes("weight", new AttributeValue.IntVal(10), new AttributeValue.IntVal(20));
        assertEquals(2, selectedNodes.size());
        assertTrue(selectedNodes.contains(a));
        assertTrue(selectedNodes.contains(c));
    }

    @Test
    public void testSelectEdgesByAttributeKeyOnly() {
        EdgeSet typedEdges = graph.selectEdges("type");
        assertEquals(2, typedEdges.size(), "Should find all edges with a 'type' attribute");
        assertTrue(typedEdges.contains(ab));
        assertTrue(typedEdges.contains(bc));
        assertFalse(typedEdges.contains(ca));
    }

    @Test
    public void testSelectEdgesByAttributeKeyAndValue() {
        EdgeSet friendEdges = graph.selectEdges("type", new AttributeValue.StringVal("friend"));
        assertEquals(1, friendEdges.size(), "Should find only edges with type='friend'");
        assertTrue(friendEdges.contains(ab));

        // Test multiple values
        EdgeSet selectedEdges = graph.selectEdges("type", new AttributeValue.StringVal("friend"), new AttributeValue.StringVal("enemy"));
        assertEquals(2, selectedEdges.size());
        assertTrue(selectedEdges.contains(ab));
        assertTrue(selectedEdges.contains(bc));
    }
}
