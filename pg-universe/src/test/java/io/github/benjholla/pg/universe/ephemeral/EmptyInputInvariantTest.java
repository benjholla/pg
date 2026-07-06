package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class EmptyInputInvariantTest {
    private EphemeralGraph graph;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();
        Node a = (EphemeralNode) new EphemeralGraph().createNode(); Node b = (EphemeralNode) new EphemeralGraph().createNode();
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(a, b));
    }

    @Test
    public void testEmptyInputsToTraversals() {
        Graph empty = new EphemeralGraph();

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
