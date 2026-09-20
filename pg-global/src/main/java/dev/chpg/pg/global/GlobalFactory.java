package dev.chpg.pg.global;

import dev.chpg.pg.api.Factory;

/**
 * A factory interface for creating globalweight graph components.
 * <p>
 * <b>What it represents:</b> A specialized builder interface for instantiating elements natively compatible with {@link GlobalGraph}s.
 * <p>
 * <b>Why it exists:</b> To encapsulate the creation of simple, isolated, in-memory reference implementations without exposing their constructors directly.
 * <p>
 * <b>When to use it:</b> Use {@code GlobalFactory} when you need a simple, self-contained reference graph without the overhead of transactions or multiple universes.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating global graph elements (e.g., {@code globalGraph.factory().createNode()}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> All components instantiated by a {@code GlobalFactory} are strictly compatible with {@code GlobalGraph}s and rely on simple 0-indexed dense primitive identities.
 * <p>
 * <b>Thread safety:</b> Factory implementations for GlobalGraph use atomic sequence generators, making instantiation itself thread-safe (though mutating the subsequent graph is not).
 * <p>
 * <b>Performance characteristics:</b> Instantiation is highly optimized for O(1) performance using an internal {@code AtomicInteger} sequence.
 */
public interface GlobalFactory extends Factory {

}
