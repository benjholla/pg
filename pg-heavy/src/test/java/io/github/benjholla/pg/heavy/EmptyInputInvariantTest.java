package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class EmptyInputInvariantTest {
    private HeavyGraph graph;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        Node a = (HeavyNode) new HeavyGraph().createNode(); Node b = (HeavyNode) new HeavyGraph().createNode();
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(a, b));
    }

    @Test
    public void testEmptyInputsToTraversals() {
        Graph empty = new HeavyGraph();

        Graph result1 = graph.forward(empty);
        assertTrue(result1.isEmpty(), "forward(empty) should be empty");

        Graph result2 = graph.reverse(empty);
        assertTrue(result2.isEmpty(), "reverse(empty) should be empty");

        Graph result3 = graph.forwardStep(empty);
        assertTrue(result3.isEmpty(), "forwardStep(empty) should be empty");

        Graph result4 = graph.reverseStep(empty);
        assertTrue(result4.isEmpty(), "reverseStep(empty) should be empty");
    }
}
