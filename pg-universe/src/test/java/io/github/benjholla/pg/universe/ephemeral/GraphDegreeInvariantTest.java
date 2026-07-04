package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Node.NodeDirection;

public class GraphDegreeInvariantTest {

    private EphemeralGraph graph;
    private Node a, b, c, d, e;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();
        a = new EphemeralNode(); b = new EphemeralNode(); c = new EphemeralNode();
        d = new EphemeralNode(); e = new EphemeralNode();

        graph.addEdge(new EphemeralEdge(a, b));
        graph.addEdge(new EphemeralEdge(b, c));
        graph.addEdge(new EphemeralEdge(c, d));
        graph.addEdge(new EphemeralEdge(d, b)); // cycle
        graph.addEdge(new EphemeralEdge(d, e));
        graph.addEdge(new EphemeralEdge(e, a)); // another cycle
        graph.addEdge(new EphemeralEdge(a, a)); // self-loop
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
        EphemeralGraph emptyGraph = new EphemeralGraph();
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
