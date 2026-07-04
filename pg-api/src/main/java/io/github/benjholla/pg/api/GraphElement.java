package io.github.benjholla.pg.api;

/**
 * Represents a fundamental entity within the property graph, such as a {@link Node} or an {@link Edge}.
 * <p>
 * A {@code GraphElement} is defined by three core properties:
 * <ul>
 * <li><b>Identity:</b> Each element has a unique primitive {@code int} identifier. This enforces memory efficiency,
 * avoids object-header overhead, and allows for zero-allocation traversals via bitsets and primitive streams.</li>
 * <li><b>Tags:</b> A {@link TagSet} representing boolean markers (e.g., "Person", "knows").</li>
 * <li><b>Attributes:</b> An {@link AttributeMap} for arbitrary key-value properties.</li>
 * </ul>
 * <p>
 * <b>Important Invariant:</b> Element identity is determined strictly by its concrete implementation type
 * and its primitive {@code id()}. Two distinct JVM objects with the same type and ID refer to the same logical
 * graph element.
 */
public interface GraphElement {
    TagSet tags();
    AttributeMap attributes();
    int id();
}
