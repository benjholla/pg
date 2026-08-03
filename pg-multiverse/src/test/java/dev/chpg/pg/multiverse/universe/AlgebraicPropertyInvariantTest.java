package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

/**
 * Validates fundamental set-theoretic algebraic properties of UniverseGraph operations
 * (union, intersection, difference) using randomized graph generation.
 *
 * Ensures operations hold over a wide variety of topologies (cycles, disjoint parts, empty subsets).
 */
public class AlgebraicPropertyInvariantTest {

    private final Random random = new Random(42); // Deterministic seed for reproducible property tests

    private void assertGraphsEqual(Graph expected, Graph actual) {
        assertEquals(expected.nodes().size(), actual.nodes().size(), "Node count mismatch");
        assertEquals(expected.edges().size(), actual.edges().size(), "Edge count mismatch");
        assertTrue(expected.nodes().containsAll(actual.nodes()), "Nodes mismatch");
        assertTrue(actual.nodes().containsAll(expected.nodes()), "Nodes mismatch");
        assertTrue(expected.edges().containsAll(actual.edges()), "Edges mismatch");
        assertTrue(actual.edges().containsAll(expected.edges()), "Edges mismatch");
    }

    private Graph generateRandomUniverseGraph(int numNodes, double edgeProbability) {
        Universe universe = new Universe();
        EphemeralFactory factory = new EphemeralGraph(universe).factory();
        Graph ephGraph = factory.createGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            Node node = factory.createNode();
            nodes.add(node);
            ephGraph.addNode(node);
        }

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (random.nextDouble() < edgeProbability) {
                    ephGraph.addEdge(factory.createEdge(nodes.get(i), nodes.get(j)));
                }
            }
        }
        return universe.promote((EphemeralGraph) ephGraph);
    }

    private Graph pickRandomSubgraph(Graph source, double inclusionProbability) {
        Graph subgraph = source.difference(source); // Empty subgraph
        for (Node n : source.nodes()) {
            if (random.nextDouble() < inclusionProbability) {
                subgraph = subgraph.union(n);
            }
        }
        for (Edge e : source.edges()) {
            if (subgraph.nodes().contains(e.from()) && subgraph.nodes().contains(e.to())) {
                if (random.nextDouble() < inclusionProbability) {
                    subgraph = subgraph.union(e);
                }
            }
        }
        return subgraph;
    }

    @RepeatedTest(10)
    public void testUnionCommutativity() {
        Graph superset = generateRandomUniverseGraph(20, 0.4);
        Graph gA = pickRandomSubgraph(superset, 0.6);
        Graph gB = pickRandomSubgraph(superset, 0.6);

        // A U B == B U A
        Graph aUnionB = gA.union(gB);
        Graph bUnionA = gB.union(gA);
        assertGraphsEqual(aUnionB, bUnionA);
    }

    @RepeatedTest(10)
    public void testIntersectionCommutativity() {
        Graph superset = generateRandomUniverseGraph(20, 0.4);
        Graph gA = pickRandomSubgraph(superset, 0.6);
        Graph gB = pickRandomSubgraph(superset, 0.6);

        // A ∩ B == B ∩ A
        Graph aIntersectB = gA.intersection(gB);
        Graph bIntersectA = gB.intersection(gA);
        assertGraphsEqual(aIntersectB, bIntersectA);
    }

    @RepeatedTest(10)
    public void testDifferenceCascadingBehavior() {
        Graph superset = generateRandomUniverseGraph(20, 0.4);
        Graph gA = pickRandomSubgraph(superset, 0.8);
        Graph gB = pickRandomSubgraph(superset, 0.6);

        Graph aMinusB = gA.difference(gB);
        Graph aMinusAMinusB = gA.difference(aMinusB);
        Graph aIntB = gA.intersection(gB);

        assertTrue(aMinusAMinusB.nodes().containsAll(aIntB.nodes()), "A \\ (A \\ B) should contain all nodes of A ∩ B");
        assertTrue(aMinusAMinusB.edges().containsAll(aIntB.edges()), "A \\ (A \\ B) should contain all edges of A ∩ B");
    }
}
