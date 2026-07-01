package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates fundamental path-reversibility axioms for directed graphs.
 */
public class GraphPathReversibilityInvariantTest {

    private PropertyGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new PropertyGraph();
        a = new Node(); b = new Node(); c = new Node();
        d = new Node(); e = new Node();

        // Create a path a -> b -> c -> d -> e
        graph.add(new Edge(a, b));
        graph.add(new Edge(b, c));
        graph.add(new Edge(c, d));
        graph.add(new Edge(d, e));

        // Add a cycle for complexity
        graph.add(new Edge(c, a));
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
