package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

/**
 * Validates fundamental path-reversibility axioms for directed graphs.
 */
public class GraphPathReversibilityInvariantTest {

    private EphemeralGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();
        a = new EphemeralNode(); b = new EphemeralNode(); c = new EphemeralNode();
        d = new EphemeralNode(); e = new EphemeralNode();

        // Create a path a -> b -> c -> d -> e
        graph.addEdge(new EphemeralEdge(a, b));
        graph.addEdge(new EphemeralEdge(b, c));
        graph.addEdge(new EphemeralEdge(c, d));
        graph.addEdge(new EphemeralEdge(d, e));

        // Add a cycle for complexity
        graph.addEdge(new EphemeralEdge(c, a));
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
