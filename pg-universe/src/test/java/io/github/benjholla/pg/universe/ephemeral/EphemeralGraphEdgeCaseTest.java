package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

public class EphemeralGraphEdgeCaseTest {
    private static final EphemeralGraph factory = new EphemeralGraph();


    @Test
    public void testUnionWithNull() {
        EphemeralGraph graph = new EphemeralGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }
}
