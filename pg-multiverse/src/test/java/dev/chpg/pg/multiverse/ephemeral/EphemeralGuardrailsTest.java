package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EphemeralGuardrailsTest {

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
