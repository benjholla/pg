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
        a.attributes().put("color", AttributeValue.value("red"));
        a.attributes().put("weight", AttributeValue.value(10));

        b = factory.createNode();
        b.attributes().put("color", AttributeValue.value("blue"));

        c = factory.createNode();
        c.attributes().put("weight", AttributeValue.value(20));

        ab = factory.createEdge(a, b);
        ab.attributes().put("type", AttributeValue.value("friend"));

        bc = factory.createEdge(b, c);
        bc.attributes().put("type", AttributeValue.value("enemy"));

        ca = factory.createEdge(c, a);
        ca.attributes().put("distance", AttributeValue.value(50));

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);
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
        NodeSet redNodes = graph.nodes().filter("color", AttributeValue.value("red"));
        assertEquals(1, redNodes.size(), "Should find only nodes with color='red'");
        assertTrue(redNodes.contains(a));

        // Test multiple values
        NodeSet selectedNodes = graph.nodes().filter("weight", AttributeValue.value(10), AttributeValue.value(20));
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
        EdgeSet friendEdges = graph.selectEdges("type", AttributeValue.value("friend"));
        assertEquals(1, friendEdges.size(), "Should find only edges with type='friend'");
        assertTrue(friendEdges.contains(ab));

        // Test multiple values
        EdgeSet selectedEdges = graph.selectEdges("type", AttributeValue.value("friend"), AttributeValue.value("enemy"));
        assertEquals(2, selectedEdges.size());
        assertTrue(selectedEdges.contains(ab));
        assertTrue(selectedEdges.contains(bc));
    }

    @Test
    public void testSelectNodesEmptyGraph() {
        Graph empty = factory.createGraph();
        NodeSet emptySet = empty.selectNodes("color");
        assertTrue(emptySet.isEmpty(), "Selecting from empty graph should return empty set");
    }

    @Test
    public void testSelectEdgesEmptyGraph() {
        Graph empty = factory.createGraph();
        EdgeSet emptySet = empty.selectEdges("type");
        assertTrue(emptySet.isEmpty(), "Selecting edges from empty graph should return empty set");
    }

    @Test
    public void testSelectNodesNonExistentKey() {
        NodeSet emptySet = graph.selectNodes("nonexistent_key");
        assertTrue(emptySet.isEmpty(), "Selecting non-existent key should return empty set");
    }

    @Test
    public void testSelectEdgesNonExistentKey() {
        EdgeSet emptySet = graph.selectEdges("nonexistent_key");
        assertTrue(emptySet.isEmpty(), "Selecting edges with non-existent key should return empty set");
    }

    @Test
    public void testSelectNodesNullKey() {
        NodeSet emptySet = graph.selectNodes(null);
        assertTrue(emptySet.isEmpty(), "Selecting with null key should return empty set safely");
    }

    @Test
    public void testSelectEdgesNullKey() {
        EdgeSet emptySet = graph.selectEdges(null);
        assertTrue(emptySet.isEmpty(), "Selecting edges with null key should return empty set safely");
    }

    @Test
    public void testSelectEdgesNullKeyAndValues() {
        EdgeSet emptySet = graph.selectEdges(null, AttributeValue.value("friend"));
        assertTrue(emptySet.isEmpty(), "Selecting edges with null key but valid value should return empty set safely");

        EdgeSet emptySet2 = graph.selectEdges("type", (AttributeValue[]) null);
        assertTrue(emptySet2.isEmpty(), "Selecting edges with valid key but null value array should return empty set safely");
    }
}