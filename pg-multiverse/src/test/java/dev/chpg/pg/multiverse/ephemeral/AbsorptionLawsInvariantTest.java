package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

/**
 * Validates the absorption laws for graph set operations:
 * 1. A U (A ∩ B) = A
 * 2. A ∩ (A U B) = A
 */
public class AbsorptionLawsInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph gA, gB;

    @BeforeEach
    public void setUp() {
        Node a = factory.createNode();
        Node b = factory.createNode();
        Node c = factory.createNode();

        Edge ab = factory.createEdge(a, b);
        Edge bc = factory.createEdge(b, c);

        gA = factory.createGraph(a, b);
        gA.addEdge(ab);

        gB = factory.createGraph(b, c);
        gB.addEdge(bc);
    }

    private void assertGraphsEqual(Graph expected, Graph actual) {
        assertEquals(expected.nodes().size(), actual.nodes().size(), "Node count mismatch");
        assertEquals(expected.edges().size(), actual.edges().size(), "Edge count mismatch");
        assertTrue(expected.nodes().containsAll(actual.nodes()), "Nodes mismatch");
        assertTrue(actual.nodes().containsAll(expected.nodes()), "Nodes mismatch");
        assertTrue(expected.edges().containsAll(actual.edges()), "Edges mismatch");
        assertTrue(actual.edges().containsAll(expected.edges()), "Edges mismatch");
    }

    @Test
    public void testAbsorptionLaw1() {
        // A U (A ∩ B) = A
        Graph aIntB = gA.intersection(gB);
        Graph aUnion_aIntB = gA.union(aIntB);
        assertGraphsEqual(gA, aUnion_aIntB);
    }

    @Test
    public void testAbsorptionLaw2() {
        // A ∩ (A U B) = A
        Graph aUnionB = gA.union(gB);
        Graph aInt_aUnionB = gA.intersection(aUnionB);
        assertGraphsEqual(gA, aInt_aUnionB);
    }
}
