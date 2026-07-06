package io.github.benjholla.pg.universe.ephemeral;

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

    private EphemeralGraph graph;
    private Node a, b, c, d, e, f, g;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();
        a = (EphemeralNode) new EphemeralGraph().createNode(); b = (EphemeralNode) new EphemeralGraph().createNode(); c = (EphemeralNode) new EphemeralGraph().createNode(); d = (EphemeralNode) new EphemeralGraph().createNode();
        e = (EphemeralNode) new EphemeralGraph().createNode(); f = (EphemeralNode) new EphemeralGraph().createNode(); g = (EphemeralNode) new EphemeralGraph().createNode();

        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(a, b));
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(b, c));
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(c, d));

        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(e, f));
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(f, g));
    }

    private void assertIsSubgraph(Graph subgraph, Graph supergraph) {
        assertTrue(supergraph.nodes().containsAll(subgraph.nodes()), "Supergraph should contain all nodes of subgraph");
        assertTrue(supergraph.edges().containsAll(subgraph.edges()), "Supergraph should contain all edges of subgraph");
    }

    @Test
    public void testForwardMonotonicity() {
        NodeSet subset = new EphemeralNodeSet(b);
        NodeSet superset = new EphemeralNodeSet(b, f);

        Graph forwardSubset = graph.forward(subset);
        Graph forwardSuperset = graph.forward(superset);

        assertIsSubgraph(forwardSubset, forwardSuperset);
    }

    @Test
    public void testReverseMonotonicity() {
        NodeSet subset = new EphemeralNodeSet(c);
        NodeSet superset = new EphemeralNodeSet(c, g);

        Graph reverseSubset = graph.reverse(subset);
        Graph reverseSuperset = graph.reverse(superset);

        assertIsSubgraph(reverseSubset, reverseSuperset);
    }

    @Test
    public void testForwardStepMonotonicity() {
        NodeSet subset = new EphemeralNodeSet(b);
        NodeSet superset = new EphemeralNodeSet(b, e);

        Graph forwardStepSubset = graph.forwardStep(subset);
        Graph forwardStepSuperset = graph.forwardStep(superset);

        assertIsSubgraph(forwardStepSubset, forwardStepSuperset);
    }

    @Test
    public void testReverseStepMonotonicity() {
        NodeSet subset = new EphemeralNodeSet(c);
        NodeSet superset = new EphemeralNodeSet(c, g);

        Graph reverseStepSubset = graph.reverseStep(subset);
        Graph reverseStepSuperset = graph.reverseStep(superset);

        assertIsSubgraph(reverseStepSubset, reverseStepSuperset);
    }

    @Test
    public void testBetweenMonotonicity() {
        NodeSet fromSubset = new EphemeralNodeSet(a);
        NodeSet fromSuperset = new EphemeralNodeSet(a, e);
        NodeSet toSubset = new EphemeralNodeSet(d);
        NodeSet toSuperset = new EphemeralNodeSet(d, g);

        Graph betweenSubset = graph.between(fromSubset, toSubset);
        Graph betweenSuperset = graph.between(fromSuperset, toSuperset);

        assertIsSubgraph(betweenSubset, betweenSuperset);
    }
}
