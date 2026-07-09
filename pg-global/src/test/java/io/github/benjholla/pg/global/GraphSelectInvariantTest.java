package io.github.benjholla.pg.global;

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
 * Validates the behavior of select operations via nodes().filter() and edges().filter().
 */
public class GraphSelectInvariantTest {

    private GlobalGraph graph;
    private Node a, b, c;
    private Edge ab, bc, ca;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();

        a = new GlobalNode();
        a.attributes().put("color", new AttributeValue.StringVal("red"));
        a.attributes().put("weight", new AttributeValue.IntVal(10));

        b = new GlobalNode();
        b.attributes().put("color", new AttributeValue.StringVal("blue"));

        c = new GlobalNode();
        c.attributes().put("weight", new AttributeValue.IntVal(20));

        ab = new GlobalEdge(a, b);
        ab.attributes().put("type", new AttributeValue.StringVal("friend"));

        bc = new GlobalEdge(b, c);
        bc.attributes().put("type", new AttributeValue.StringVal("enemy"));

        ca = new GlobalEdge(c, a);
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
