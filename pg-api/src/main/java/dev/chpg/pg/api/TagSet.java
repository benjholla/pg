package dev.chpg.pg.api;

import java.util.Set;

/**
 * A specialized set for managing boolean markers (tags) on a {@link GraphElement}.
 * <p>
 * <b>What it represents:</b> A collection of string-based tags denoting logical set membership (e.g., "Person", "Vehicle").
 * <p>
 * <b>Why it exists:</b> To provide a fast, memory-efficient way to categorize graph elements without requiring heavy object hierarchies or complex attribute maps.
 * <p>
 * <b>When to use it:</b> Use {@code TagSet} to label nodes or edges with discrete types or states that are primarily checked for presence/absence.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Marking an element (e.g., {@code node.tags().add("User")}).</li>
 * <li>Checking for category membership (e.g., {@code edge.tags().contains("calls")}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> The tags stored within this set are typically plain strings. Implementations may enforce specific string formats or internally manage them via dictionaries (e.g., string pooling) to reduce memory.
 * <p>
 * <b>Thread safety:</b> Thread safety guarantees depend heavily on the concrete implementation. Modifying the underlying graph while iterating a live TagSet will likely produce a {@code ConcurrentModificationException}.
 * <p>
 * <b>Performance characteristics:</b> Checking tag presence should generally be an O(1) operation depending on the backend implementation.
 */
public interface TagSet extends Set<String> {

}
