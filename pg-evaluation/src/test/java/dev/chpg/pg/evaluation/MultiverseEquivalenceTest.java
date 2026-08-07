package dev.chpg.pg.evaluation;

import dev.chpg.pg.api.Direction;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiverseEquivalenceTest {

    private void runTest(java.util.function.Consumer<Graph> testLogic) {
        // Run on GlobalGraph
        testLogic.accept(new GlobalGraph());

        // Run on EphemeralGraph over Universe (simulating Multiverse workflow)
        Universe universe = new Universe();
        EphemeralGraph eGraph = new EphemeralGraph(universe);
        testLogic.accept(eGraph);
    }

    private Node createNode(Graph graph) {
        if (graph instanceof GlobalGraph gg) {
            return gg.createNode();
        } else if (graph instanceof EphemeralGraph eg) {
            return eg.factory().createNode();
        }
        throw new IllegalArgumentException("Unknown graph type");
    }

    private Edge createEdge(Graph graph, Node from, Node to) {
        if (graph instanceof GlobalGraph gg) {
            return gg.createEdge(from, to);
        } else if (graph instanceof EphemeralGraph eg) {
            return eg.factory().createEdge(from, to);
        }
        throw new IllegalArgumentException("Unknown graph type");
    }

    @Test
    public void testTombstoneCascadesMaskEdges() {
        runTest(graph -> {
            Node a = createNode(graph);
            Node b = createNode(graph);
            Edge e1 = createEdge(graph, a, b);

            graph.addEdge(e1);

            // If we are using EphemeralGraph, we want to test tombstoning of BASELINE edges.
            // So we need to promote the graph first if it is an EphemeralGraph.
            if (graph instanceof EphemeralGraph eg) {
                eg.universe().promote(eg);
                // After promote, `eg` is invalidated. We must create a new EphemeralGraph from the Universe
                // to test the tombstone masking in the transaction buffer!
                Graph finalGraph = new EphemeralGraph(eg.universe());
                // Find the promoted versions of our nodes/edges in the new graph
                Node promotedA = finalGraph.nodes().iterator().next();
                final int pAid = promotedA.id();
                Node promotedB = finalGraph.nodes().stream().filter(n -> n.id() != pAid).findFirst().get();
                // Ensure a is the source
                if (finalGraph.degree(promotedB, Direction.OUT) == 1) {
                    Node tmp = promotedA;
                    promotedA = promotedB;
                    promotedB = tmp;
                }

                a = promotedA;
                b = promotedB;
                e1 = finalGraph.edges().iterator().next();
                graph = finalGraph;
            }

            assertTrue(graph.nodes().contains(a));
            assertTrue(graph.edges().contains(e1));
            assertEquals(1, graph.degree(a, Direction.OUT));

            // Delete node 'a'
            graph.removeNode(a);

            // Node 'a' is gone
            assertFalse(graph.nodes().contains(a));

            // Edge 'e1' should be implicitly masked/deleted because its source was deleted
            assertFalse(graph.edges().contains(e1));

            // Out-degree of 'a' should be 0 (since it doesn't exist)
            assertEquals(0, graph.degree(a, Direction.OUT));

            // In-degree of 'b' should be 0
            assertEquals(0, graph.degree(b, Direction.IN));

            // Adjacency lists should be empty
            assertEquals(0, graph.edges(a, b).size());
            assertEquals(0, graph.edges(a, Direction.OUT).size());
            assertEquals(0, graph.edges(b, Direction.IN).size());
        });
    }

    @Test
    public void testResurrectedSelfLoops() {
        runTest(graph -> {
            Node a = createNode(graph);
            Edge loop = createEdge(graph, a, a);

            graph.addEdge(loop);

            // Promote if ephemeral to make it a baseline element
            if (graph instanceof EphemeralGraph eg) {
                eg.universe().promote(eg);
                Graph finalGraph = new EphemeralGraph(eg.universe());
                a = finalGraph.nodes().iterator().next();
                loop = finalGraph.edges().iterator().next();
                graph = finalGraph;
            }

            assertEquals(1, graph.edges().size());
            assertEquals(2, graph.degree(a, Direction.BOTH));
            assertEquals(1, graph.degree(a, Direction.IN));
            assertEquals(1, graph.degree(a, Direction.OUT));

            // Delete the edge
            graph.removeEdge(loop);

            assertEquals(0, graph.edges().size());
            assertEquals(0, graph.degree(a, Direction.BOTH));

            // Resurrect the edge
            graph.addEdge(loop);

            assertTrue(graph.edges().contains(loop));
            assertEquals(1, graph.edges().size());
            assertEquals(2, graph.degree(a, Direction.BOTH));
            assertEquals(1, graph.degree(a, Direction.IN));
            assertEquals(1, graph.degree(a, Direction.OUT));
        });
    }

    @Test
    public void testTombstoneCascadesWithInduce() {
        runTest(graph -> {
            Node a = createNode(graph);
            Node b = createNode(graph);
            Edge e1 = createEdge(graph, a, b);

            graph.addEdge(e1);

            if (graph instanceof EphemeralGraph eg) {
                eg.universe().promote(eg);
                Graph finalGraph = new EphemeralGraph(eg.universe());
                Node promotedA = finalGraph.nodes().iterator().next();
                final int pAid = promotedA.id();
                Node promotedB = finalGraph.nodes().stream().filter(n -> n.id() != pAid).findFirst().get();
                if (finalGraph.degree(promotedB, Direction.OUT) == 1) {
                    Node tmp = promotedA;
                    promotedA = promotedB;
                    promotedB = tmp;
                }
                a = promotedA;
                b = promotedB;
                e1 = finalGraph.edges().iterator().next();
                graph = finalGraph;
            }

            graph.removeNode(a);

            // Induce the edge. Since 'a' is deleted, the edge should not be inducible
            Graph induced = graph.induce(e1);
            assertEquals(0, induced.edges().size(), "Inducing a deleted edge (or edge with deleted endpoints) should yield 0 edges.");

            // Try to induce a subset of edges
            Graph inducedFromGraph = graph.induce(graph);

            // Note: EphemeralGraph.induce(Graph) delegates to graph.edges() for all edges
            // Since we deleted node 'a', the cascaded edge e1 shouldn't be in the edges() output.
            assertEquals(0, inducedFromGraph.edges().size());
        });
    }
}
