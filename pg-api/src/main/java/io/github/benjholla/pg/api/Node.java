package io.github.benjholla.pg.api;

/**
 * Represents a vertex within a property graph.
 * <p>
 * A {@code Node} serves as a fundamental building block of the graph, connecting to other nodes via
 * directed {@link Edge}s. Like all {@link GraphElement}s, it possesses a primitive {@code int} identity,
 * a {@link TagSet} for labeling, and an {@link AttributeMap} for property storage.
 */
public interface Node extends GraphElement {
    enum NodeDirection { IN, OUT; }
}
