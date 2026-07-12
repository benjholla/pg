package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;

public class GlobalGraphEdgeCaseTest {

    @Test
    public void testUnionWithNull() {
        GlobalGraph graph = new GlobalGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }
}
