package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

/**
 * Validates properties related to between traversals on the same nodes.
 */
public class SelfBetweenPropertiesInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Graph gA;
    private Node a;

    @BeforeEach
    public void setUp() {
        a = factory.createNode();
        gA = factory.createGraph(a);
    }

    @Test
    public void testBetweenSameNode() {
        Graph between = gA.between(a, a);
        assertEquals(1, between.nodes().size());
        assertEquals(0, between.edges().size());
    }

    @Test
    public void testBetweenStepSameNode() {
        Graph betweenStep = gA.betweenStep(a, a);
        assertEquals(1, betweenStep.nodes().size());
        assertEquals(0, betweenStep.edges().size());
    }
}
