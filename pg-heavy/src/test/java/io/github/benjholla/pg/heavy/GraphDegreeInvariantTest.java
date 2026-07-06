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
        a = (HeavyNode) new HeavyGraph().createNode(); b = (HeavyNode) new HeavyGraph().createNode(); c = (HeavyNode) new HeavyGraph().createNode();
        d = (HeavyNode) new HeavyGraph().createNode(); e = (HeavyNode) new HeavyGraph().createNode();

        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(a, b));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(b, c));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(c, d));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(d, b)); // cycle
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(d, e));
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(e, a)); // another cycle
        graph.addEdge((HeavyEdge) new HeavyGraph().createEdge(a, a)); // self-loop
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
