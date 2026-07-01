package io.github.benjholla.pg;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the protected factory methods in HeavyGraph that act as architectural seams
 * to support alternative implementations like UniverseGraph or EphemeralGraph.
 */
public class HeavyGraphFactoryTest extends HeavyGraph {

    @Test
    public void testNewGraphEmpty() {
        HeavyGraph g = this.newGraph();
        assertTrue(g.isEmpty());
    }

    @Test
    public void testNewGraphWithNodeVarargs() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        HeavyGraph g = this.newGraph(n1, n2);
        assertEquals(2, g.nodes().size());
        assertTrue(g.nodes().contains(n1));
        assertTrue(g.nodes().contains(n2));
    }

    @Test
    public void testNewGraphWithNodeSet() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        NodeSet nodes = new HeavyNodeSet(n1, n2);
        HeavyGraph g = this.newGraph(nodes);
        assertEquals(2, g.nodes().size());
        assertTrue(g.nodes().contains(n1));
        assertTrue(g.nodes().contains(n2));
    }

    @Test
    public void testNewGraphWithEdgeVarargs() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        HeavyGraph g = this.newGraph(e1);
        assertEquals(2, g.nodes().size());
        assertEquals(1, g.edges().size());
        assertTrue(g.edges().contains(e1));
    }

    @Test
    public void testNewGraphWithEdgeSet() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        EdgeSet edges = new HeavyEdgeSet(e1);
        HeavyGraph g = this.newGraph(edges);
        assertEquals(2, g.nodes().size());
        assertEquals(1, g.edges().size());
        assertTrue(g.edges().contains(e1));
    }

    @Test
    public void testNewGraphWithNodeSetAndEdgeSet() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Node n3 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        NodeSet nodes = new HeavyNodeSet(n1, n2, n3);
        EdgeSet edges = new HeavyEdgeSet(e1);
        HeavyGraph g = this.newGraph(nodes, edges);
        assertEquals(3, g.nodes().size());
        assertEquals(1, g.edges().size());
        assertTrue(g.nodes().contains(n3));
        assertTrue(g.edges().contains(e1));
    }

    @Test
    public void testNewGraphWithGraphVarargs() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        HeavyGraph g1 = new HeavyGraph(e1);

        Node n3 = new HeavyNode();
        Node n4 = new HeavyNode();
        Edge e2 = new HeavyEdge(n3, n4);
        HeavyGraph g2 = new HeavyGraph(e2);

        HeavyGraph g = this.newGraph(g1, g2);
        assertEquals(4, g.nodes().size());
        assertEquals(2, g.edges().size());
        assertTrue(g.edges().contains(e1));
        assertTrue(g.edges().contains(e2));
    }

    @Test
    public void testNewGraphWithGraphCollection() {
        Node n1 = new HeavyNode();
        Node n2 = new HeavyNode();
        Edge e1 = new HeavyEdge(n1, n2);
        HeavyGraph g1 = new HeavyGraph(e1);

        Node n3 = new HeavyNode();
        Node n4 = new HeavyNode();
        Edge e2 = new HeavyEdge(n3, n4);
        HeavyGraph g2 = new HeavyGraph(e2);

        Collection<Graph> graphs = Arrays.asList(g1, g2);
        HeavyGraph g = this.newGraph(graphs);
        assertEquals(4, g.nodes().size());
        assertEquals(2, g.edges().size());
        assertTrue(g.edges().contains(e1));
        assertTrue(g.edges().contains(e2));
    }
}
