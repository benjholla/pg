package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class EphemeralGraphEdgeCaseTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    @Test
    public void testUnionWithNull() {
        Graph graph = factory.createGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }
}
