package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import org.junit.jupiter.api.Test;

public class UniverseTest {

    @Test
    public void testDefaultConstructor() {
        Universe universe = new Universe();
        assertNotNull(universe.idGenerator(), "ID generator should not be null");
        assertEquals(0L, universe.modCount(), "Initial modCount should be 0");
    }

    @Test
    public void testInjectedConstructor() {
        UniverseIdGenerator generator = new UniverseIdGenerator();
        generator.incrementAndGetModCount(); // Make it non-zero for testing

        Universe universe = new Universe(generator);
        assertSame(generator, universe.idGenerator(), "Should return injected ID generator");
        assertEquals(1L, universe.modCount(), "Should reflect injected generator's modCount");
    }

    @Test
    public void testUniverseIdUniqueness() {
        Universe u1 = new Universe();
        Universe u2 = new Universe();
        Universe u3 = new Universe(new UniverseIdGenerator());


        assertNotEquals(u1.universeId(), u2.universeId(), "Universe IDs must be unique");
        assertNotEquals(u1.universeId(), u3.universeId(), "Universe IDs must be unique");
        assertNotEquals(u2.universeId(), u3.universeId(), "Universe IDs must be unique");

        assertEquals(u2.universeId(), u1.universeId() + 1, "Universe IDs should be monotonically increasing");
        assertEquals(u3.universeId(), u2.universeId() + 1, "Universe IDs should be monotonically increasing");
    }

    @Test
    public void testNullInjectedConstructor() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new Universe(null);
        });
        assertEquals("IdGenerator cannot be null", exception.getMessage());
    }

    @Test
    public void testIncrementModCount() {
        Universe universe = new Universe();
        assertEquals(0L, universe.modCount());

        long newCount = universe.incrementModCount();
        assertEquals(1L, newCount);
        assertEquals(1L, universe.modCount());

        universe.incrementModCount();
        assertEquals(2L, universe.modCount());
    }

    @Test
    public void testPromoteThrowsException() {
        Universe universe = new Universe();

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            universe.promote(new EphemeralGraph());
        });

        assertEquals("TODO: Implement in Phase 4 (Promotion)", exception.getMessage());
    }

    @Test
    public void testToString() {
        Universe universe = new Universe();
        assertEquals("Universe[id=" + universe.universeId() + ", allocatedNodes=0, allocatedEdges=0]", universe.toString());

        universe.idGenerator().createNodeId();
        universe.idGenerator().createNodeId();
        universe.idGenerator().createEdgeId();

        assertEquals("Universe[id=" + universe.universeId() + ", allocatedNodes=2, allocatedEdges=1]", universe.toString());
    }
}
