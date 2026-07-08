package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

/**
 * Validates the monotonicity invariant of graph traversals.
 * Monotonicity states that if A is a subset of B, then traversal(A) should be a subset of traversal(B).
 */
public class GraphMonotonicityInvariantTest {

    private GlobalGraph graph;
    private Node a, b, c, d, e, f, g;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        a = new GlobalNode(); b = new GlobalNode(); c = new GlobalNode(); d = new GlobalNode();
        e = new GlobalNode(); f = new GlobalNode(); g = new GlobalNode();

        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(c, d));

        graph.addEdge(new GlobalEdge(e, f));
        graph.addEdge(new GlobalEdge(f, g));
    }

    private void assertIsSubgraph(Graph subgraph, Graph supergraph) {
        assertTrue(supergraph.nodes().containsAll(subgraph.nodes()), "Supergraph should contain all nodes of subgraph");
        assertTrue(supergraph.edges().containsAll(subgraph.edges()), "Supergraph should contain all edges of subgraph");
    }

    @Test
    public void testForwardMonotonicity() {
        NodeSet subset = new GlobalNodeSet(b);
        NodeSet superset = new GlobalNodeSet(b, f);

        Graph forwardSubset = graph.forward(subset);
        Graph forwardSuperset = graph.forward(superset);

        assertIsSubgraph(forwardSubset, forwardSuperset);
    }

    @Test
    public void testReverseMonotonicity() {
        NodeSet subset = new GlobalNodeSet(c);
        NodeSet superset = new GlobalNodeSet(c, g);

        Graph reverseSubset = graph.reverse(subset);
        Graph reverseSuperset = graph.reverse(superset);

        assertIsSubgraph(reverseSubset, reverseSuperset);
    }

    @Test
    public void testForwardStepMonotonicity() {
        NodeSet subset = new GlobalNodeSet(b);
        NodeSet superset = new GlobalNodeSet(b, e);

        Graph forwardStepSubset = graph.forwardStep(subset);
        Graph forwardStepSuperset = graph.forwardStep(superset);

        assertIsSubgraph(forwardStepSubset, forwardStepSuperset);
    }

    @Test
    public void testReverseStepMonotonicity() {
        NodeSet subset = new GlobalNodeSet(c);
        NodeSet superset = new GlobalNodeSet(c, g);

        Graph reverseStepSubset = graph.reverseStep(subset);
        Graph reverseStepSuperset = graph.reverseStep(superset);

        assertIsSubgraph(reverseStepSubset, reverseStepSuperset);
    }

    @Test
    public void testBetweenMonotonicity() {
        NodeSet fromSubset = new GlobalNodeSet(a);
        NodeSet fromSuperset = new GlobalNodeSet(a, e);
        NodeSet toSubset = new GlobalNodeSet(d);
        NodeSet toSuperset = new GlobalNodeSet(d, g);

        Graph betweenSubset = graph.between(fromSubset, toSubset);
        Graph betweenSuperset = graph.between(fromSuperset, toSuperset);

        assertIsSubgraph(betweenSubset, betweenSuperset);
    }
}
