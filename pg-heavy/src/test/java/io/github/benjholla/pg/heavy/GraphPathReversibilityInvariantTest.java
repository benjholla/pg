package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

/**
 * Validates fundamental path-reversibility axioms for directed graphs.
 */
public class GraphPathReversibilityInvariantTest {

    private HeavyGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        a = (HeavyNode) new HeavyGraph().createNode(); b = (HeavyNode) new HeavyGraph().createNode(); c = (HeavyNode) new HeavyGraph().createNode();
        d = (HeavyNode) new HeavyGraph().createNode(); e = (HeavyNode) new HeavyGraph().createNode();

        // Create a path a -> b -> c -> d -> e
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(a, b));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(b, c));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(c, d));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(d, e));

        // Add a cycle for complexity
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(c, a));
    }

    @Test
    public void testForwardImpliesReverseInclusion() {
        // If v is in forward(u), then u must be in reverse(v)

        // Check for every pair of nodes
        for (Node u : graph.nodes()) {
            Graph forwardU = graph.forward(u);
            for (Node v : forwardU.nodes()) {
                Graph reverseV = graph.reverse(v);
                assertTrue(reverseV.nodes().contains(u),
                    "If v is reachable from u, u must be reachable in reverse from v");
            }
        }
    }

    @Test
    public void testForwardStepImpliesReverseStepInclusion() {
        // If v is in forwardStep(u), then u must be in reverseStep(v)

        // Check for every pair of nodes
        for (Node u : graph.nodes()) {
            Graph forwardStepU = graph.forwardStep(u);
            for (Node v : forwardStepU.nodes()) {
                Graph reverseStepV = graph.reverseStep(v);
                assertTrue(reverseStepV.nodes().contains(u),
                    "If v is one step reachable from u, u must be one step reachable in reverse from v");
            }
        }
    }
}
