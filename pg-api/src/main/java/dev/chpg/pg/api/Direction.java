package dev.chpg.pg.api;

/**
 * Specifies the traversal direction relative to a node in the graph.
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
