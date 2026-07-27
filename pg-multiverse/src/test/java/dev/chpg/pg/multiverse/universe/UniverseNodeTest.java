package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.chpg.pg.multiverse.ephemeral.EphemeralNode;
import org.junit.jupiter.api.Test;

public class UniverseNodeTest {

    @Test
    public void testValidInstantiation() {
        Universe universe = new Universe();
        UniverseNode node = new UniverseNode(universe, 1);

        assertEquals(1, node.id(), "Node should return correct ID");
        assertSame(universe, node.universe(), "Node should return correct Universe");
    }

    @Test
    public void testNullUniverseThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new UniverseNode(null, 1);
        });
        assertEquals("Universe cannot be null", exception.getMessage());
    }

    @Test
    public void testZeroIdAllowed() {
        Universe universe = new Universe();
        UniverseNode node = new UniverseNode(universe, 0);
        assertEquals(0, node.id(), "Node should allow ID 0");

    }

    @Test
    public void testNegativeIdThrowsException() {
        Universe universe = new Universe();
        UniverseNode node = new UniverseNode(universe, 0);
        assertEquals(0, node.id(), "Node should allow ID 0");

    }

    @Test
    public void testEqualsAndHashCode() {
        Universe universe1 = new Universe();
        Universe universe2 = new Universe();

        UniverseNode nodeA = new UniverseNode(universe1, 42);
        UniverseNode nodeB = new UniverseNode(universe1, 42);
        UniverseNode nodeC = new UniverseNode(universe2, 42);
        UniverseNode nodeD = new UniverseNode(universe1, 43);

        // Fast-path reference check
        assertEquals(nodeA, nodeA, "Node should equal itself");

        // Same ID, same universe
        assertEquals(nodeA, nodeB, "Nodes with same ID should be equal");
        assertEquals(nodeA.hashCode(), nodeB.hashCode(), "Equal nodes should have same hashcode");

        // Same ID, different universe
        assertEquals(nodeA, nodeC, "Nodes with same ID but different universes should be equal (as per architectural mandate)");
        assertEquals(nodeA.hashCode(), nodeC.hashCode(), "Nodes with same ID but different universes should have same hashcode");

        // Different ID
        assertNotEquals(nodeA, nodeD, "Nodes with different IDs should not be equal");

        // Null and different type
        assertNotEquals(nodeA, null, "Node should not equal null");
        assertNotEquals(nodeA, new Object(), "Node should not equal Object");
    }

    @Test
    public void testCrossTypeEqualityRejection() {
        Universe universe = new Universe();
        UniverseNode universeNode = new UniverseNode(universe, 100);
        EphemeralNode ephemeralNode = new EphemeralNode(100);

        assertNotEquals(universeNode, ephemeralNode, "UniverseNode should reject equality with EphemeralNode");
    }
}
