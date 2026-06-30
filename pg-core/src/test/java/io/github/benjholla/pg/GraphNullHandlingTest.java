package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraphNullHandlingTest {
    private PropertyGraph graph;

    @BeforeEach
    public void setup() {
        graph = new PropertyGraph();
    }

    @Test
    public void testUnionNullArray() {
        assertThrows(NullPointerException.class, () -> graph.union((Graph[]) null));
    }

    @Test
    public void testUnionArrayWithNulls() {
        assertThrows(NullPointerException.class, () -> graph.union(new Graph[]{null}));
    }
}
