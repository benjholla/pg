package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Factory;

/**
 * A factory interface for creating ephemeral graph components.
 * <p>
 * <b>What it represents:</b> A specialized builder interface for instantiating elements natively compatible with {@link EphemeralGraph}s.
 * <p>
 * <b>Why it exists:</b> To encapsulate the creation of multiverse-compatible, transaction-aware components (like {@link EphemeralNode} or {@link EphemeralEdge}) without exposing their constructors directly.
 * <p>
 * <b>When to use it:</b> Use {@code EphemeralFactory} when you need to strictly constrain instantiation to ephemeral components bound to a specific {@link dev.chpg.pg.multiverse.universe.Universe}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating multiverse elements via an ephemeral graph (e.g., {@code ephemeralGraph.factory().createNode()}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> All components instantiated by an {@code EphemeralFactory} are inextricably bound to the specific {@code Universe} and {@code EphemeralIdGenerator} of the {@code EphemeralGraph} that produced the factory. Sandbox mismatches will violently reject element mixing.
 * <p>
 * <b>Thread safety:</b> Factory implementations for EphemeralGraph are thread-safe and utilize atomic ID generators under the hood.
 * <p>
 * <b>Performance characteristics:</b> Instantiations bypass complex allocations and rely on flyweight patterns and primitive ID reservation, operating in O(1) time.
 */
public interface EphemeralFactory extends Factory {

}
