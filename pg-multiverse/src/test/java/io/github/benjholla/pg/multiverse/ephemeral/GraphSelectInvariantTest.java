package io.github.benjholla.pg.multiverse.ephemeral;

import io.github.benjholla.pg.api.Graph;

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
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph graph;
    private Node a, b, c;
    private Edge ab, bc, ca;

    @BeforeEach
    public void setUp() {
        graph = factory.createGraph();

        a = factory.createNode();
        a.attributes().put("color", new AttributeValue.StringVal("red"));
        a.attributes().put("weight", new AttributeValue.IntVal(10));

        b = factory.createNode();
        b.attributes().put("color", new AttributeValue.StringVal("blue"));

        c = factory.createNode();
        c.attributes().put("weight", new AttributeValue.IntVal(20));

        ab = factory.createEdge(a, b);
        ab.attributes().put("type", new AttributeValue.StringVal("friend"));

        bc = factory.createEdge(b, c);
        bc.attributes().put("type", new AttributeValue.StringVal("enemy"));

        ca = factory.createEdge(c, a);
        ca.attributes().put("distance", new AttributeValue.IntVal(50));

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);
    }

    @Test
    public void testSelectNodesByAttributeKeyOnly() {
        NodeSet coloredNodes = graph.nodes().filter("color");
        assertEquals(2, coloredNodes.size(), "Should find all nodes with a 'color' attribute");
        assertTrue(coloredNodes.contains(a));
        assertTrue(coloredNodes.contains(b));
        assertFalse(coloredNodes.contains(c));
    }

    @Test
    public void testSelectNodesByAttributeKeyAndValue() {
        NodeSet redNodes = graph.nodes().filter("color", new AttributeValue.StringVal("red"));
        assertEquals(1, redNodes.size(), "Should find only nodes with color='red'");
        assertTrue(redNodes.contains(a));

        // Test multiple values
        NodeSet selectedNodes = graph.nodes().filter("weight", new AttributeValue.IntVal(10), new AttributeValue.IntVal(20));
        assertEquals(2, selectedNodes.size());
        assertTrue(selectedNodes.contains(a));
        assertTrue(selectedNodes.contains(c));
    }

    @Test
    public void testSelectEdgesByAttributeKeyOnly() {
        EdgeSet typedEdges = graph.edges().filter("type");
        assertEquals(2, typedEdges.size(), "Should find all edges with a 'type' attribute");
        assertTrue(typedEdges.contains(ab));
        assertTrue(typedEdges.contains(bc));
        assertFalse(typedEdges.contains(ca));
    }

    @Test
    public void testSelectEdgesByAttributeKeyAndValue() {
        EdgeSet friendEdges = graph.edges().filter("type", new AttributeValue.StringVal("friend"));
        assertEquals(1, friendEdges.size(), "Should find only edges with type='friend'");
        assertTrue(friendEdges.contains(ab));

        // Test multiple values
        EdgeSet selectedEdges = graph.edges().filter("type", new AttributeValue.StringVal("friend"), new AttributeValue.StringVal("enemy"));
        assertEquals(2, selectedEdges.size());
        assertTrue(selectedEdges.contains(ab));
        assertTrue(selectedEdges.contains(bc));
    }
}
