package io.github.benjholla.pg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LargeGraphInvariantTest {

    @Test
    public void testDeepLinearGraphTraversal() {
        // Create a large linearly connected graph to verify we don't hit StackOverflowError
        // due to recursive implementations
        int size = 10000;
        PropertyGraph graph = new PropertyGraph();

        Node[] nodes = new Node[size];
        for(int i=0; i<size; i++) {
            nodes[i] = new Node();
            if(i > 0) {
                graph.add(new Edge(nodes[i-1], nodes[i]));
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
