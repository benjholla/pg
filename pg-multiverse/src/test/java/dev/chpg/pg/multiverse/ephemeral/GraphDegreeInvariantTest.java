package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Node.NodeDirection;

public class GraphDegreeInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = factory.createGraph();
        a = factory.createNode(); b = factory.createNode(); c = factory.createNode();
        d = factory.createNode(); e = factory.createNode();

        graph.addEdge(factory.createEdge(a, b));
        graph.addEdge(factory.createEdge(b, c));
        graph.addEdge(factory.createEdge(c, d));
        graph.addEdge(factory.createEdge(d, b)); // cycle
        graph.addEdge(factory.createEdge(d, e));
        graph.addEdge(factory.createEdge(e, a)); // another cycle
        graph.addEdge(factory.createEdge(a, a)); // self-loop
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
        Graph emptyGraph = factory.createGraph();
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
