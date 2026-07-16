package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

/**
 * Validates fundamental properties of predecessors and successors in graphs.
 * Ensuring duality: a ∈ predecessors(b) ⇔ b ∈ successors(a).
 */
public class PredecessorSuccessorInvariantTest {

    private static final EphemeralFactory factory = new EphemeralGraph().factory();
    private final Random random = new Random(42); // Deterministic seed for reproducible property tests

    private Graph generateRandomGraph(int numNodes, double edgeProbability) {
        Graph graph = factory.createGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            Node node = factory.createNode();
            nodes.add(node);
            graph.addNode(node);
        }

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (random.nextDouble() < edgeProbability) {
                    graph.addEdge(factory.createEdge(nodes.get(i), nodes.get(j)));
                }
            }
        }
        return graph;
    }

    @RepeatedTest(10)
    public void testPredecessorSuccessorDuality() {
        Graph graph = generateRandomGraph(20, 0.2);

        for (Node a : graph.nodes()) {
            for (Node b : graph.nodes()) {
                boolean aInPredecessorsOfB = graph.predecessors(b).contains(a);
                boolean bInSuccessorsOfA = graph.successors(a).contains(b);
                assertEquals(aInPredecessorsOfB, bInSuccessorsOfA,
                    "Duality violation: a in predecessors(b) should equal b in successors(a)");
            }
        }
    }

    @RepeatedTest(10)
    public void testForwardStepAndSuccessorsRelationship() {
        Graph graph = generateRandomGraph(20, 0.2);

        for (Node node : graph.nodes()) {
            Graph forwardStep = graph.forwardStep(node);
            NodeSet successors = graph.successors(node);

            // Forward step includes the origin node and all successors
            assertTrue(forwardStep.nodes().contains(node));
            assertTrue(forwardStep.nodes().containsAll(successors));

            // The size should match origin + successors (if origin is not in successors, then size + 1)
            int expectedSize = successors.size() + (successors.contains(node) ? 0 : 1);
            assertEquals(expectedSize, forwardStep.nodes().size());
        }
    }

    @RepeatedTest(10)
    public void testReverseStepAndPredecessorsRelationship() {
        Graph graph = generateRandomGraph(20, 0.2);

        for (Node node : graph.nodes()) {
            Graph reverseStep = graph.reverseStep(node);
            NodeSet predecessors = graph.predecessors(node);

            // Reverse step includes the origin node and all predecessors
            assertTrue(reverseStep.nodes().contains(node));
            assertTrue(reverseStep.nodes().containsAll(predecessors));

            // The size should match origin + predecessors (if origin is not in predecessors, then size + 1)
            int expectedSize = predecessors.size() + (predecessors.contains(node) ? 0 : 1);
            assertEquals(expectedSize, reverseStep.nodes().size());
        }
    }
}
