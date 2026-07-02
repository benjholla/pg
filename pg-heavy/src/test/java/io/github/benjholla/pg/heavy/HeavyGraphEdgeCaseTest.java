package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HeavyGraphEdgeCaseTest {

    @Test
    public void testUnionWithNull() {
        HeavyGraph graph = new HeavyGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }
}
