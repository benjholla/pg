package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class DisjointGraphInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    private Graph graphA;
    private Graph graphB;

    private Node a1, a2, b1, b2;

    @BeforeEach
    public void setUp() {
        a1 = factory.createNode();
        a2 = factory.createNode();
        graphA = factory.createGraph(a1, a2);
        graphA.addEdge(factory.createEdge(a1, a2));

        b1 = factory.createNode();
        b2 = factory.createNode();
        graphB = factory.createGraph(b1, b2);
        graphB.addEdge(factory.createEdge(b1, b2));
    }

    @Test
    public void testDisjointIntersectionIsEmpty() {
        Graph intersection = graphA.intersection(graphB);
        assertTrue(intersection.nodes().isEmpty());
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
        Graph unionGraph = factory.createGraph(graphA.nodes(), graphA.edges());
        unionGraph = (EphemeralGraph) unionGraph.union(graphB);

        // Between a1 and b2 should be completely empty since there is no path between graphA and graphB
        Graph between = unionGraph.between(a1, b2);
        assertTrue(between.nodes().isEmpty());
    }
}
