package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphAdjacentInvariantTest {

    private static final EphemeralFactory factory = new EphemeralGraph().factory();
    private Graph graph;
    private Node a, b, c, d;

    @BeforeEach
    public void setUp() {
        graph = factory.createGraph();
        a = factory.createNode();
        b = factory.createNode();
        c = factory.createNode();
        d = factory.createNode();
    }

    @Test
    public void testAdjacentWithSingleEdge() {
        graph.addEdge(factory.createEdge(a, b));

        assertTrue(graph.adjacent(a, b));
        assertFalse(graph.adjacent(b, a)); // Directed edge
    }

    @Test
    public void testAdjacentWithMultipleEdges() {
        graph.addEdge(factory.createEdge(a, b));
        graph.addEdge(factory.createEdge(a, b));
        graph.addEdge(factory.createEdge(a, b));

        assertTrue(graph.adjacent(a, b));
    }

    @Test
    public void testNotAdjacent() {
        graph.addEdge(factory.createEdge(a, b));
        graph.addEdge(factory.createEdge(b, c));

        // transitive but not adjacent
        assertFalse(graph.adjacent(a, c));
        assertFalse(graph.adjacent(c, a));
    }

    @Test
    public void testAdjacentToSelf() {
        graph.addEdge(factory.createEdge(a, a));

        assertTrue(graph.adjacent(a, a));
    }

    @Test
    public void testEdgesMethod() {
        Edge e1 = factory.createEdge(a, b);
        Edge e2 = factory.createEdge(a, b);
        Edge e3 = factory.createEdge(a, c);

        graph.addEdge(e1);
        graph.addEdge(e2);
        graph.addEdge(e3);

        assertEquals(2, graph.edges(a, b).size());
        assertTrue(graph.edges(a, b).contains(e1));
        assertTrue(graph.edges(a, b).contains(e2));

        assertEquals(1, graph.edges(a, c).size());
        assertTrue(graph.edges(a, c).contains(e3));

        assertEquals(0, graph.edges(b, a).size());
        assertEquals(0, graph.edges(c, a).size());
        assertEquals(0, graph.edges(b, c).size());
    }

    @Test
    public void testNodesNotInGraph() {
        graph.addEdge(factory.createEdge(a, b));

        // Node d is not in the graph
        assertFalse(graph.adjacent(a, d));
        assertFalse(graph.adjacent(d, b));
        assertFalse(graph.adjacent(c, d)); // neither are in graph (since c wasn't added)

        assertEquals(0, graph.edges(a, d).size());
        assertEquals(0, graph.edges(d, b).size());
        assertEquals(0, graph.edges(c, d).size());
    }
}
