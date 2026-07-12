package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

/**
 * Validates properties related to between traversals on the same nodes.
 */
public class SelfBetweenPropertiesInvariantTest {

    private GlobalGraph gA;
    private GlobalNode a;

    @BeforeEach
    public void setUp() {
        a = new GlobalNode();
        gA = new GlobalGraph(a);
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
