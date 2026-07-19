package dev.chpg.pg.multiverse.ephemeral;

/** Guardrails for Ephemeral operations. */
public class EphemeralGuardrails {

    /**
     * TEMPORARY GUARDRAIL: Enforces Option 1 strict local scope.
     * Throws if an ID is positive (Universe scope).
     * @param id the id to check
     */
    public static void requireLocalId(int id) {
        if (id >= 0) {
            throw new IllegalArgumentException(
                "Foreign links to Universe IDs (" + id + ") are strictly disabled until Universe integration."
            );
        }
    }
}
