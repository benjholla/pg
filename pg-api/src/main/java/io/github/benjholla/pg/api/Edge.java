package io.github.benjholla.pg.api;

/**
 * Represents a directed connection between two {@link Node}s in a property graph.
 * <p>
 * An {@code Edge} strictly dictates a direction, originating at the {@link #from()} node and terminating
 * at the {@link #to()} node. Like all {@link GraphElement}s, it possesses a primitive {@code int} identity,
 * a {@link TagSet} for labeling (e.g., relationship type), and an {@link AttributeMap} for property storage.
 * <p>
 * <b>Important Invariant:</b> Edges cannot exist independently of their endpoints. Attempting to add an edge
 * to a graph implicitly requires (or will add) its corresponding {@code from} and {@code to} nodes. Removing
 * either connected node will cascade and result in the removal of the edge.
 */
public interface Edge extends GraphElement {
    Node from();
    Node to();
}
