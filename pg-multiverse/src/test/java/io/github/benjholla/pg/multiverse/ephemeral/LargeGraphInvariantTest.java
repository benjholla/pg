package io.github.benjholla.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class LargeGraphInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    @Test
    public void testDeepLinearGraphTraversal() {
        // Create a large linearly connected graph to verify we don't hit StackOverflowError
        // due to recursive implementations
        int size = 10000;
        Graph graph = factory.createGraph();

        Node[] nodes = new Node[size];
        for(int i=0; i<size; i++) {
            nodes[i] = factory.createNode();
            if(i > 0) {
                graph.addEdge(factory.createEdge(nodes[i-1], nodes[i]));
            }
        }

        // Assert we can forward traverse the entire graph
        Graph fullForward = graph.forward(nodes[0]);
        assertEquals(size, fullForward.nodes().size());
        assertEquals(size - 1, fullForward.edges().size());

        // Assert we can reverse traverse the entire graph
        Graph fullReverse = graph.reverse(nodes[size - 1]);
        assertEquals(size, fullReverse.nodes().size());
        assertEquals(size - 1, fullReverse.edges().size());

        // Assert we can calculate between the entire graph
        Graph fullBetween = graph.between(nodes[0], nodes[size - 1]);
        assertEquals(size, fullBetween.nodes().size());
        assertEquals(size - 1, fullBetween.edges().size());
    }
}
