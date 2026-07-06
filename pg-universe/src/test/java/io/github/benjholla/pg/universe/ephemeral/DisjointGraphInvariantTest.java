package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class DisjointGraphInvariantTest {
    private EphemeralGraph graphA;
    private EphemeralGraph graphB;

    private Node a1, a2, b1, b2;

    @BeforeEach
    public void setUp() {
        a1 = (EphemeralNode) new EphemeralGraph().createNode();
        a2 = (EphemeralNode) new EphemeralGraph().createNode();
        graphA = (EphemeralGraph) new EphemeralGraph().createGraph(a1, a2);
        graphA.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(a1, a2));

        b1 = (EphemeralNode) new EphemeralGraph().createNode();
        b2 = (EphemeralNode) new EphemeralGraph().createNode();
        graphB = (EphemeralGraph) new EphemeralGraph().createGraph(b1, b2);
        graphB.addEdge((EphemeralEdge) new EphemeralGraph().createEdge(b1, b2));
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
        EphemeralGraph unionGraph = (EphemeralGraph) new EphemeralGraph().createGraph(graphA.nodes(), graphA.edges());
        unionGraph = (EphemeralGraph) unionGraph.union(graphB);

        // Between a1 and b2 should be completely empty since there is no path between graphA and graphB
        Graph between = unionGraph.between(a1, b2);
        assertTrue(between.isEmpty());
    }
}
