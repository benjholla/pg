package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Node.NodeDirection;

public class GraphDegreeInvariantTest {

    private HeavyGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new HeavyGraph();
        a = new HeavyNode(); b = new HeavyNode(); c = new HeavyNode();
        d = new HeavyNode(); e = new HeavyNode();

        graph.add(new HeavyEdge(a, b));
        graph.add(new HeavyEdge(b, c));
        graph.add(new HeavyEdge(c, d));
        graph.add(new HeavyEdge(d, b)); // cycle
        graph.add(new HeavyEdge(d, e));
        graph.add(new HeavyEdge(e, a)); // another cycle
        graph.add(new HeavyEdge(a, a)); // self-loop
    }

    @Test
    public void testHandshakingLemma() {
        int totalInDegree = 0;
        int totalOutDegree = 0;

        for (Node node : graph.nodes()) {
            totalInDegree += graph.edges(node, NodeDirection.IN).size();
            totalOutDegree += graph.edges(node, NodeDirection.OUT).size();
        }

        int totalEdges = graph.edges().size();

        assertEquals(totalEdges, totalInDegree, "Sum of in-degrees must equal total number of edges");
        assertEquals(totalEdges, totalOutDegree, "Sum of out-degrees must equal total number of edges");
    }

    @Test
    public void testHandshakingLemmaOnEmptyGraph() {
        HeavyGraph emptyGraph = new HeavyGraph();
        int totalInDegree = 0;
        int totalOutDegree = 0;

        for (Node node : emptyGraph.nodes()) {
            totalInDegree += emptyGraph.edges(node, NodeDirection.IN).size();
            totalOutDegree += emptyGraph.edges(node, NodeDirection.OUT).size();
        }

        assertEquals(0, totalInDegree);
        assertEquals(0, totalOutDegree);
        assertEquals(0, emptyGraph.edges().size());
    }
}
