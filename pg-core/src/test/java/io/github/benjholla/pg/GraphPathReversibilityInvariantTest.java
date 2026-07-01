package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates fundamental path-reversibility axioms for directed graphs.
 */
public class GraphPathReversibilityInvariantTest {

    private HeavyGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        a = new HeavyNode(); b = new HeavyNode(); c = new HeavyNode();
        d = new HeavyNode(); e = new HeavyNode();

        // Create a path a -> b -> c -> d -> e
        graph.add(new HeavyEdge(a, b));
        graph.add(new HeavyEdge(b, c));
        graph.add(new HeavyEdge(c, d));
        graph.add(new HeavyEdge(d, e));

        // Add a cycle for complexity
        graph.add(new HeavyEdge(c, a));
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
