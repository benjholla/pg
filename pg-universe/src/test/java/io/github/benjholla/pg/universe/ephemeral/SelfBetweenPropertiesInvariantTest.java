package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates properties related to between traversals on the same nodes.
 */
public class SelfBetweenPropertiesInvariantTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    private EphemeralGraph gA;
    private EphemeralNode a;

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
