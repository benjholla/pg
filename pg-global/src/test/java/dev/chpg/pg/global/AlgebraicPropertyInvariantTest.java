package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

/**
 * Validates fundamental set-theoretic algebraic properties of Graph operations
 * (union, intersection, difference) using randomized graph generation.
 *
 * Ensures operations hold over a wide variety of topologies (cycles, disjoint parts, empty subsets).
 */
public class AlgebraicPropertyInvariantTest {

    private Random random;

    @BeforeEach
    public void setUp() {
        random = new Random(42); // Deterministic seed for reproducible property tests
    }

    private void assertGraphsEqual(Graph expected, Graph actual) {
        assertEquals(expected.nodes().size(), actual.nodes().size(), "Node count mismatch");
        assertEquals(expected.edges().size(), actual.edges().size(), "Edge count mismatch");
        assertTrue(expected.nodes().containsAll(actual.nodes()), "Nodes mismatch");
        assertTrue(actual.nodes().containsAll(expected.nodes()), "Nodes mismatch");
        assertTrue(expected.edges().containsAll(actual.edges()), "Edges mismatch");
        assertTrue(actual.edges().containsAll(expected.edges()), "Edges mismatch");
    }

    private Graph generateRandomGraph(int numNodes, double edgeProbability) {
        GlobalGraph graph = new GlobalGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            GlobalNode node = new GlobalNode();
            nodes.add(node);
            graph.addNode(node);
        }

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (random.nextDouble() < edgeProbability) {
                    graph.addEdge(new GlobalEdge(nodes.get(i), nodes.get(j)));
                }
            }
        }
        return graph;
    }

    private Graph pickRandomSubgraph(Graph source, double inclusionProbability) {
        GlobalGraph subgraph = new GlobalGraph();
        for (Node n : source.nodes()) {
            if (random.nextDouble() < inclusionProbability) {
                subgraph.addNode(n);
            }
        }
        for (dev.chpg.pg.api.Edge e : source.edges()) {
            if (subgraph.nodes().contains(e.from()) && subgraph.nodes().contains(e.to())) {
                if (random.nextDouble() < inclusionProbability) {
                    subgraph.addEdge(e);
                }
            }
        }
        return subgraph;
    }

    @RepeatedTest(10)
    public void testUnionCommutativity() {
        Graph gA = generateRandomGraph(10, 0.3);
        Graph gB = generateRandomGraph(10, 0.3);

        // A U B == B U A
        Graph aUnionB = gA.union(gB);
        Graph bUnionA = gB.union(gA);
        assertGraphsEqual(aUnionB, bUnionA);
    }

    @RepeatedTest(10)
    public void testIntersectionCommutativity() {
        Graph superset = generateRandomGraph(20, 0.4);
        Graph gA = pickRandomSubgraph(superset, 0.6);
        Graph gB = pickRandomSubgraph(superset, 0.6);

        // A ∩ B == B ∩ A
        Graph aIntersectB = gA.intersection(gB);
        Graph bIntersectA = gB.intersection(gA);
        assertGraphsEqual(aIntersectB, bIntersectA);
    }

    @RepeatedTest(10)
    public void testDifferenceCascadingBehavior() {
        Graph superset = generateRandomGraph(20, 0.4);
        Graph gA = pickRandomSubgraph(superset, 0.8);
        Graph gB = pickRandomSubgraph(superset, 0.6);

        Graph aMinusB = gA.difference(gB);
        Graph aMinusAMinusB = gA.difference(aMinusB);
        Graph aIntB = gA.intersection(gB);

        // Because a node removal cascades to edge removal,
        // A \ B can remove edges from A that were NOT in B (because their endpoints were in B).
        // Therefore, A \ (A \ B) will retain those edges, whereas A ∩ B will NOT contain them
        // if they weren't in B in the first place.
        // The proper relation for property graphs is that A \ (A \ B) is a SUPERSET (or equal) to A ∩ B.

        assertTrue(aMinusAMinusB.nodes().containsAll(aIntB.nodes()), "A \\ (A \\ B) should contain all nodes of A ∩ B");
        assertTrue(aMinusAMinusB.edges().containsAll(aIntB.edges()), "A \\ (A \\ B) should contain all edges of A ∩ B");
    }
}
