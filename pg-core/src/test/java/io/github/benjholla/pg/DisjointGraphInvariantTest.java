package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DisjointGraphInvariantTest {
    private PropertyGraph graphA;
    private PropertyGraph graphB;

    private Node a1, a2, b1, b2;

    @BeforeEach
    public void setUp() {
        a1 = new Node();
        a2 = new Node();
        graphA = new PropertyGraph(a1, a2);
        graphA.add(new Edge(a1, a2));

        b1 = new Node();
        b2 = new Node();
        graphB = new PropertyGraph(b1, b2);
        graphB.add(new Edge(b1, b2));
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
        PropertyGraph unionGraph = new PropertyGraph(graphA.nodes(), graphA.edges());
        unionGraph = (PropertyGraph) unionGraph.union(graphB);

        // Between a1 and b2 should be completely empty since there is no path between graphA and graphB
        Graph between = unionGraph.between(a1, b2);
        assertTrue(between.isEmpty());
    }
}
