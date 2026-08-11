package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EphemeralEdgeTest {
    @Test
    public void testEphemeralEdge() {
        Universe universe = new Universe();
        EphemeralNode n1 = new EphemeralNode(universe, -1);
        EphemeralNode n2 = new EphemeralNode(universe, -2);

        EphemeralEdge e = new EphemeralEdge(universe, -1, n1, n2);

        assertEquals(-1, e.id());
        assertEquals(universe, e.universe());
        assertEquals(e.hashCode(), e.hashCode());

        assertTrue(e.equals(e));
        assertFalse(e.equals(null));
        assertFalse(e.equals(new Object()));

        EphemeralEdge e2 = new EphemeralEdge(universe, -1, n1, n2);
        assertTrue(e.equals(e2));

        EphemeralEdge e3 = new EphemeralEdge(universe, -2, n1, n2);
        assertFalse(e.equals(e3));

        System.out.println(e.toString());
        assertEquals(n1, e.from());
        assertEquals(n2, e.to());
    }
}
