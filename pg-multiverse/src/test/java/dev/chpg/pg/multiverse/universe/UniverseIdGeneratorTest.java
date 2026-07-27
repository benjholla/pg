package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UniverseIdGeneratorTest {

    @Test
    public void testIdGenerationAndAllocationCounts() {
        UniverseIdGenerator generator = new UniverseIdGenerator();

        // Initial state
        assertEquals(0, generator.allocatedNodeCount(), "Allocated node count should initially be 0");
        assertEquals(0, generator.allocatedEdgeCount(), "Allocated edge count should initially be 0");

        // Create node IDs
        int nodeId1 = generator.createNodeId();
        assertEquals(0, nodeId1);
        assertEquals(1, generator.allocatedNodeCount(), "Allocated node count should be 1 after creating 1 node ID");

        int nodeId2 = generator.createNodeId();
        assertEquals(1, nodeId2);
        assertEquals(2, generator.allocatedNodeCount(), "Allocated node count should be 2 after creating 2 node IDs");

        // Create edge IDs
        int edgeId1 = generator.createEdgeId();
        assertEquals(0, edgeId1);
        assertEquals(1, generator.allocatedEdgeCount(), "Allocated edge count should be 1 after creating 1 edge ID");

        int edgeId2 = generator.createEdgeId();
        assertEquals(1, edgeId2);
        assertEquals(2, generator.allocatedEdgeCount(), "Allocated edge count should be 2 after creating 2 edge IDs");
    }
}
