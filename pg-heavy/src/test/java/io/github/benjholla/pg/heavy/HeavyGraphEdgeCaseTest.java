package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Graph;

public class HeavyGraphEdgeCaseTest {

    @Test
    public void testUnionWithNull() {
        HeavyGraph graph = new HeavyGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }
}
