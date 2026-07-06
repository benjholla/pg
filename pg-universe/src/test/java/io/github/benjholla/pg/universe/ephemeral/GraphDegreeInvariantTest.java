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
        a = (EphemeralNode) new EphemeralGraph().createNode(); b = (EphemeralNode) new EphemeralGraph().createNode(); c = (EphemeralNode) new EphemeralGraph().createNode();
        d = (EphemeralNode) new EphemeralGraph().createNode(); e = (EphemeralNode) new EphemeralGraph().createNode();

        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(a, b));
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(b, c));
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(c, d));
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(d, b)); // cycle
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(d, e));
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(e, a)); // another cycle
        graph.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(a, a)); // self-loop
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
