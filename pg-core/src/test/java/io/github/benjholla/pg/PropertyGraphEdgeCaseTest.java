package io.github.benjholla.pg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyGraphEdgeCaseTest {

    @Test
    public void testUnionWithNull() {
        PropertyGraph graph = new PropertyGraph();
        assertThrows(NullPointerException.class, () -> graph.union((Graph) null));
    }
}
