package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
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
    public void testDegreeMatchesEdgeSetSize() {
        for (Node node : graph.nodes()) {
            int inDegree = graph.degree(node, NodeDirection.IN);
            int outDegree = graph.degree(node, NodeDirection.OUT);
            int bothDegree = graph.degree(node, NodeDirection.BOTH);

            assertEquals(graph.edges(node, NodeDirection.IN).size(), inDegree, "IN degree must equal IN edge set size");
            assertEquals(graph.edges(node, NodeDirection.OUT).size(), outDegree, "OUT degree must equal OUT edge set size");

            // BOTH degree must exactly equal IN + OUT degree.
            // Note: a self loop appears in both the IN set and OUT set. Since edges(node, BOTH) returns a Set,
            // the self loop is only present once in the Set, but structurally contributes 2 to the degree.
            assertEquals(inDegree + outDegree, bothDegree, "BOTH degree must equal IN + OUT degree");
        }
    }

    @Test
    public void testHandshakingLemma() {
        int totalInDegree = 0;
        int totalOutDegree = 0;
        int totalBothDegree = 0;

        for (Node node : graph.nodes()) {
            totalInDegree += graph.degree(node, NodeDirection.IN);
            totalOutDegree += graph.degree(node, NodeDirection.OUT);
            totalBothDegree += graph.degree(node, NodeDirection.BOTH);
        }

        int totalEdges = graph.edges().size();

        assertEquals(totalEdges, totalInDegree, "Sum of in-degrees must equal total number of edges");
        assertEquals(totalEdges, totalOutDegree, "Sum of out-degrees must equal total number of edges");
        assertEquals(2 * totalEdges, totalBothDegree, "Sum of BOTH degrees must equal 2 * total number of edges");
    }

    @Test
    public void testHandshakingLemmaOnEmptyGraph() {
        Graph emptyGraph = factory.createGraph();
        int totalInDegree = 0;
        int totalOutDegree = 0;
        int totalBothDegree = 0;

        for (Node node : emptyGraph.nodes()) {
            totalInDegree += emptyGraph.degree(node, NodeDirection.IN);
            totalOutDegree += emptyGraph.degree(node, NodeDirection.OUT);
            totalBothDegree += emptyGraph.degree(node, NodeDirection.BOTH);
        }

        assertEquals(0, totalInDegree);
        assertEquals(0, totalOutDegree);
        assertEquals(0, totalBothDegree);
        assertEquals(0, emptyGraph.edges().size());
    }
}
