package dev.chpg.pg.multiverse.ephemeral;

public class EphemeralGuardrails {

    /**
     * TEMPORARY GUARDRAIL: Enforces Option 1 strict local scope.
     * Throws if an ID is positive (Universe scope).
     */
    public static void requireLocalId(int id) {
        if (id >= 0) {
            throw new IllegalArgumentException(
                "Foreign links to Universe IDs (" + id + ") are strictly disabled until Universe integration."
            );
        }
    }
}
