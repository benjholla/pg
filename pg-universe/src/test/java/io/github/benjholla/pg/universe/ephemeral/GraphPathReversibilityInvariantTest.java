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
    private static final EphemeralGraph factory = new EphemeralGraph();


    private EphemeralGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();
        a = factory.createNode(); b = factory.createNode(); c = factory.createNode();
        d = factory.createNode(); e = factory.createNode();

        // Create a path a -> b -> c -> d -> e
        graph.addEdge(factory.createEdge(a, b));
        graph.addEdge(factory.createEdge(b, c));
        graph.addEdge(factory.createEdge(c, d));
        graph.addEdge(factory.createEdge(d, e));

        // Add a cycle for complexity
        graph.addEdge(factory.createEdge(c, a));
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
