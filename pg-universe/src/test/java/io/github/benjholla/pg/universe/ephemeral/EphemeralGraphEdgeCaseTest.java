package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

public class EphemeralGraphEdgeCaseTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    @Test
    public void testUnionWithNull() {
        Graph graph = new EphemeralGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }
}
