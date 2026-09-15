package dev.chpg.pg.api;

/**
 * Specifies the traversal direction relative to a node in the graph.
 * <p>
 * <b>What it represents:</b> An enumeration defining the topological orientation for querying incident edges and neighboring nodes.
 * <p>
 * <b>Why it exists:</b> To provide a standardized, explicit mechanism for specifying traversal intent, eliminating ambiguous boolean flags (e.g., {@code isOutgoing}) and supporting bidirectional queries.
 * <p>
 * <b>When to use it:</b> Use {@code Direction} when querying node degrees, evaluating adjacencies, or stepping through the graph structure.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>{@link #IN}: Traverses incoming edges (edges where a node is the {@link Edge#to()} destination).</li>
 * <li>{@link #OUT}: Traverses outgoing edges (edges where a node is the {@link Edge#from()} source).</li>
 * <li>{@link #BOTH}: Traverses all incident edges regardless of direction.</li>
 * </ul>
 */
public enum Direction {
    /** Incoming edges */
    IN,
    /** Outgoing edges */
    OUT,
    /** Both incoming and outgoing edges */
    BOTH;
}
