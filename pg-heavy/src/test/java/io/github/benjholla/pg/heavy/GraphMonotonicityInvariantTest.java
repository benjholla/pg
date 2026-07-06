package io.github.benjholla.pg.heavy;

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

    private HeavyGraph graph;
    private Node a, b, c, d, e, f, g;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        a = new HeavyNode(); b = new HeavyNode(); c = new HeavyNode(); d = new HeavyNode();
        e = new HeavyNode(); f = new HeavyNode(); g = new HeavyNode();

        graph.addEdge(new HeavyEdge(a, b));
        graph.addEdge(new HeavyEdge(b, c));
        graph.addEdge(new HeavyEdge(c, d));

        graph.addEdge(new HeavyEdge(e, f));
        graph.addEdge(new HeavyEdge(f, g));
    }

    private void assertIsSubgraph(Graph subgraph, Graph supergraph) {
        assertTrue(supergraph.nodes().containsAll(subgraph.nodes()), "Supergraph should contain all nodes of subgraph");
        assertTrue(supergraph.edges().containsAll(subgraph.edges()), "Supergraph should contain all edges of subgraph");
    }

    @Test
    public void testForwardMonotonicity() {
        NodeSet subset = new HeavyNodeSet(b);
        NodeSet superset = new HeavyNodeSet(b, f);

        Graph forwardSubset = graph.forward(subset);
        Graph forwardSuperset = graph.forward(superset);

        assertIsSubgraph(forwardSubset, forwardSuperset);
    }

    @Test
    public void testReverseMonotonicity() {
        NodeSet subset = new HeavyNodeSet(c);
        NodeSet superset = new HeavyNodeSet(c, g);

        Graph reverseSubset = graph.reverse(subset);
        Graph reverseSuperset = graph.reverse(superset);

        assertIsSubgraph(reverseSubset, reverseSuperset);
    }

    @Test
    public void testForwardStepMonotonicity() {
        NodeSet subset = new HeavyNodeSet(b);
        NodeSet superset = new HeavyNodeSet(b, e);

        Graph forwardStepSubset = graph.forwardStep(subset);
        Graph forwardStepSuperset = graph.forwardStep(superset);

        assertIsSubgraph(forwardStepSubset, forwardStepSuperset);
    }

    @Test
    public void testReverseStepMonotonicity() {
        NodeSet subset = new HeavyNodeSet(c);
        NodeSet superset = new HeavyNodeSet(c, g);

        Graph reverseStepSubset = graph.reverseStep(subset);
        Graph reverseStepSuperset = graph.reverseStep(superset);

        assertIsSubgraph(reverseStepSubset, reverseStepSuperset);
    }

    @Test
    public void testBetweenMonotonicity() {
        NodeSet fromSubset = new HeavyNodeSet(a);
        NodeSet fromSuperset = new HeavyNodeSet(a, e);
        NodeSet toSubset = new HeavyNodeSet(d);
        NodeSet toSuperset = new HeavyNodeSet(d, g);

        Graph betweenSubset = graph.between(fromSubset, toSubset);
        Graph betweenSuperset = graph.between(fromSuperset, toSuperset);

        assertIsSubgraph(betweenSubset, betweenSuperset);
    }
}
