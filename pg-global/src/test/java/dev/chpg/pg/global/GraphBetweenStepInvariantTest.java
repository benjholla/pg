package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphBetweenStepInvariantTest {
    private GlobalGraph graph;
    private Node a, b, c, d, e, f;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        a = new GlobalNode(); b = new GlobalNode(); c = new GlobalNode();
        d = new GlobalNode(); e = new GlobalNode(); f = new GlobalNode();

        graph.addEdge(new GlobalEdge(a, b));
        graph.addEdge(new GlobalEdge(b, c));
        graph.addEdge(new GlobalEdge(c, d));
        graph.addEdge(new GlobalEdge(d, b)); // cycle b-c-d-b
        graph.addEdge(new GlobalEdge(e, f));
    }

    private void assertGraphsEqual(Graph expected, Graph actual) {
        assertEquals(expected.nodes().size(), actual.nodes().size(), "Node count mismatch");
        assertEquals(expected.edges().size(), actual.edges().size(), "Edge count mismatch");
        assertTrue(expected.nodes().containsAll(actual.nodes()), "Nodes mismatch");
        assertTrue(actual.nodes().containsAll(expected.nodes()), "Nodes mismatch");
        assertTrue(expected.edges().containsAll(actual.edges()), "Edges mismatch");
        assertTrue(actual.edges().containsAll(expected.edges()), "Edges mismatch");
    }

    @Test
    public void testBetweenStepIsIntersectionOfForwardStepAndReverseStep() {
        Node[] nodes = {a, b, c, d, e, f};

        for (Node u : nodes) {
            for (Node v : nodes) {
                Graph forwardStepU = graph.forwardStep(u);
                Graph reverseStepV = graph.reverseStep(v);

                // Node overload
                Graph betweenStepUV = graph.betweenStep(u, v);
                Graph intersection = forwardStepU.intersection(reverseStepV);
                assertGraphsEqual(intersection, betweenStepUV);

                // NodeSet overload
                Graph betweenStepSets = graph.betweenStep(graph.singleton(u), graph.singleton(v));
                assertGraphsEqual(intersection, betweenStepSets);

                // Graph overload
                Graph fromGraph = graph.intersection(u); // creates a graph with just node u
                Graph toGraph = graph.intersection(v);   // creates a graph with just node v
                Graph betweenStepGraphs = graph.betweenStep(fromGraph, toGraph);
                assertGraphsEqual(intersection, betweenStepGraphs);
            }
        }
    }
}
