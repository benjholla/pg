package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

/**
 * Validates properties related to between traversals on the same nodes.
 */
public class SelfBetweenPropertiesInvariantTest {

    private HeavyGraph gA;
    private HeavyNode a;

    @BeforeEach
    public void setUp() {
        a = new HeavyNode();
        gA = new HeavyGraph(a);
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
