package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Node.NodeDirection;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNode;

public class UniverseGraphDegreeInvariantTest {

    private Universe universe;
    private Graph graph;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        EphemeralGraph eg = new EphemeralGraph();

        EphemeralNode a = eg.createNode();
        EphemeralNode b = eg.createNode();
        EphemeralNode c = eg.createNode();
        EphemeralNode d = eg.createNode();
        EphemeralNode e = eg.createNode();

        eg.addEdge(eg.createEdge(a, b));
        eg.addEdge(eg.createEdge(b, c));
        eg.addEdge(eg.createEdge(c, d));
        eg.addEdge(eg.createEdge(d, b)); // cycle
        eg.addEdge(eg.createEdge(d, e));
        eg.addEdge(eg.createEdge(e, a)); // another cycle
        eg.addEdge(eg.createEdge(a, a)); // self-loop

        graph = universe.promote(eg);
    }

    @Test
    public void testDegreeMatchesEdgeSetSize() {
        for (Node node : graph.nodes()) {
            int inDegree = graph.degree(node, NodeDirection.IN);
            int outDegree = graph.degree(node, NodeDirection.OUT);
            int bothDegree = graph.degree(node, NodeDirection.BOTH);

            assertEquals(graph.edges(node, NodeDirection.IN).size(), inDegree, "IN degree must equal IN edge set size");
            assertEquals(graph.edges(node, NodeDirection.OUT).size(), outDegree, "OUT degree must equal OUT edge set size");

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
        Graph emptyGraph = universe.promote(new EphemeralGraph());
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
