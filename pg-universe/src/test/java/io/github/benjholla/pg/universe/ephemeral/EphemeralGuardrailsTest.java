package io.github.benjholla.pg.universe.ephemeral;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EphemeralGuardrailsTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    @Test
    public void testRequireLocalId() {
        assertThrows(IllegalArgumentException.class, () -> EphemeralGuardrails.requireLocalId(0));
        assertThrows(IllegalArgumentException.class, () -> EphemeralGuardrails.requireLocalId(1));
        assertThrows(IllegalArgumentException.class, () -> EphemeralGuardrails.requireLocalId(100));
        assertDoesNotThrow(() -> EphemeralGuardrails.requireLocalId(-1));
        assertDoesNotThrow(() -> EphemeralGuardrails.requireLocalId(-100));
    }

    @Test
    public void testConstructor() {
        assertDoesNotThrow(() -> new EphemeralGuardrails());
    }
}
