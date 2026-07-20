package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class EmptyInputInvariantTest {
    private GlobalGraph graph;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        Node a = new GlobalNode(); Node b = new GlobalNode();
        graph.addEdge(new GlobalEdge(a, b));
    }

    @Test
    public void testEmptyInputsToTraversals() {
        Graph empty = new GlobalGraph();

        Graph result1 = graph.forward(empty);
        assertTrue(result1.nodes().isEmpty(), "forward(empty) should be empty");

        Graph result2 = graph.reverse(empty);
        assertTrue(result2.nodes().isEmpty(), "reverse(empty) should be empty");

        Graph result3 = graph.forwardStep(empty);
        assertTrue(result3.nodes().isEmpty(), "forwardStep(empty) should be empty");

        Graph result4 = graph.reverseStep(empty);
        assertTrue(result4.nodes().isEmpty(), "reverseStep(empty) should be empty");
    }
}
