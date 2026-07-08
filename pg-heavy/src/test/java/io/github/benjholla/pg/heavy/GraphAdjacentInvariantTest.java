package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class GraphAdjacentInvariantTest {

    private HeavyGraph graph;
    private Node a, b, c, d;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        a = new HeavyNode();
        b = new HeavyNode();
        c = new HeavyNode();
        d = new HeavyNode();
    }

    @Test
    public void testAdjacentWithSingleEdge() {
        graph.addEdge(new HeavyEdge(a, b));

        assertTrue(graph.adjacent(a, b));
        assertFalse(graph.adjacent(b, a)); // Directed edge
    }

    @Test
    public void testAdjacentWithMultipleEdges() {
        graph.addEdge(new HeavyEdge(a, b));
        graph.addEdge(new HeavyEdge(a, b));
        graph.addEdge(new HeavyEdge(a, b));

        assertTrue(graph.adjacent(a, b));
    }

    @Test
    public void testNotAdjacent() {
        graph.addEdge(new HeavyEdge(a, b));
        graph.addEdge(new HeavyEdge(b, c));

        // transitive but not adjacent
        assertFalse(graph.adjacent(a, c));
        assertFalse(graph.adjacent(c, a));
    }

    @Test
    public void testAdjacentToSelf() {
        graph.addEdge(new HeavyEdge(a, a));

        assertTrue(graph.adjacent(a, a));
    }

    @Test
    public void testEdgesMethod() {
        Edge e1 = new HeavyEdge(a, b);
        Edge e2 = new HeavyEdge(a, b);
        Edge e3 = new HeavyEdge(a, c);

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
        graph.addEdge(new HeavyEdge(a, b));

        // Node d is not in the graph
        assertFalse(graph.adjacent(a, d));
        assertFalse(graph.adjacent(d, b));
        assertFalse(graph.adjacent(c, d)); // neither are in graph (since c wasn't added)

        assertEquals(0, graph.edges(a, d).size());
        assertEquals(0, graph.edges(d, b).size());
        assertEquals(0, graph.edges(c, d).size());
    }
}
