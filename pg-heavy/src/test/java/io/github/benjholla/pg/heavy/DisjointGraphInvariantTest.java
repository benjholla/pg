package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class DisjointGraphInvariantTest {
    private HeavyGraph graphA;
    private HeavyGraph graphB;

    private Node a1, a2, b1, b2;

    @BeforeEach
    public void setUp() {
        a1 = (HeavyNode) new HeavyGraph().createNode();
        a2 = (HeavyNode) new HeavyGraph().createNode();
        graphA = (HeavyGraph) new HeavyGraph().createGraph(a1, a2);
        graphA.addEdge((HeavyEdge) new HeavyGraph().createEdge(a1, a2));

        b1 = (HeavyNode) new HeavyGraph().createNode();
        b2 = (HeavyNode) new HeavyGraph().createNode();
        graphB = (HeavyGraph) new HeavyGraph().createGraph(b1, b2);
        graphB.addEdge((HeavyEdge) new HeavyGraph().createEdge(b1, b2));
    }

    @Test
    public void testDisjointIntersectionIsEmpty() {
        Graph intersection = graphA.intersection(graphB);
        assertTrue(intersection.isEmpty());
        assertEquals(0, intersection.nodes().size());
        assertEquals(0, intersection.edges().size());
    }

    @Test
    public void testDisjointDifferenceIdentity() {
        Graph diffA = graphA.difference(graphB);
        assertEquals(2, diffA.nodes().size());
        assertEquals(1, diffA.edges().size());

        Graph diffB = graphB.difference(graphA);
        assertEquals(2, diffB.nodes().size());
        assertEquals(1, diffB.edges().size());
    }

    @Test
    public void testBetweenDisjointSubgraphsIsEmpty() {
        HeavyGraph unionGraph = (HeavyGraph) new HeavyGraph().createGraph(graphA.nodes(), graphA.edges());
        unionGraph = (HeavyGraph) unionGraph.union(graphB);

        // Between a1 and b2 should be completely empty since there is no path between graphA and graphB
        Graph between = unionGraph.between(a1, b2);
        assertTrue(between.isEmpty());
    }
}
