package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.chpg.pg.multiverse.ephemeral.EphemeralEdge;
import org.junit.jupiter.api.Test;

public class UniverseEdgeTest {

    @Test
    public void testValidInstantiation() {
        Universe universe = new Universe();
        UniverseEdge edge = new UniverseEdge(universe, 1);

        assertEquals(1, edge.id(), "Edge should return correct ID");
        assertSame(universe, edge.universe(), "Edge should return correct Universe");
    }

    @Test
    public void testNullUniverseThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new UniverseEdge(null, 1);
        });
        assertEquals("Universe cannot be null", exception.getMessage());
    }

    @Test
    public void testZeroIdThrowsException() {
        Universe universe = new Universe();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new UniverseEdge(universe, 0);
        });
        assertTrue(exception.getMessage().contains("strictly positive"), "Should validate strictly positive ID");
    }

    @Test
    public void testNegativeIdThrowsException() {
        Universe universe = new Universe();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new UniverseEdge(universe, -5);
        });
        assertTrue(exception.getMessage().contains("strictly positive"), "Should validate strictly positive ID");
    }

    @Test
    public void testEqualsAndHashCode() {
        Universe universe1 = new Universe();
        Universe universe2 = new Universe();

        UniverseEdge edgeA = new UniverseEdge(universe1, 42);
        UniverseEdge edgeB = new UniverseEdge(universe1, 42);
        UniverseEdge edgeC = new UniverseEdge(universe2, 42);
        UniverseEdge edgeD = new UniverseEdge(universe1, 43);

        // Fast-path reference check
        assertEquals(edgeA, edgeA, "Edge should equal itself");

        // Same ID, same universe
        assertEquals(edgeA, edgeB, "Edges with same ID should be equal");
        assertEquals(edgeA.hashCode(), edgeB.hashCode(), "Equal edges should have same hashcode");

        // Same ID, different universe
        assertEquals(edgeA, edgeC, "Edges with same ID but different universes should be equal");
        assertEquals(edgeA.hashCode(), edgeC.hashCode(), "Edges with same ID but different universes should have same hashcode");

        // Different ID
        assertNotEquals(edgeA, edgeD, "Edges with different IDs should not be equal");

        // Null and different type
        assertNotEquals(edgeA, null, "Edge should not equal null");
        assertNotEquals(edgeA, new Object(), "Edge should not equal Object");
    }

    @Test
    public void testCrossTypeEqualityRejection() {
        Universe universe = new Universe();
        UniverseEdge universeEdge = new UniverseEdge(universe, 100);

        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph graph = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.api.Node n1 = graph.createNode();
        dev.chpg.pg.api.Node n2 = graph.createNode();
        EphemeralEdge ephemeralEdge = graph.createEdge(n1, n2);

        assertNotEquals(universeEdge, ephemeralEdge, "UniverseEdge should reject equality with EphemeralEdge");
    }
}
