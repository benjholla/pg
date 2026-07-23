package dev.chpg.pg.api;

/**
 * <b>What it represents:</b> A fundamental entity within the property graph, such as a {@link Node} or an {@link Edge}.
 * <p>
 * <b>Why it exists:</b> To provide a unified abstraction for all topological entities within the graph, guaranteeing they share an identity, tags, and properties.
 * <p>
 * <b>When to use it:</b> Primarily as a foundational interface. Code should generally interact with the more specific {@link Node} or {@link Edge} interfaces.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Extracting the primitive ID for routing or mapping.</li>
 * <li>Checking common tags or properties shared between nodes and edges.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> The interface defines no explicit thread safety guarantees. Thread safety depends on the concrete implementation.
 * <p>
 * <b>Performance characteristics:</b> ID retrieval is strictly O(1) and guaranteed to be a primitive int without object overhead.
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
    /**
     * Returns the boolean tags associated with this element.
     *
     * @return the tag set
     */
    TagSet tags();

    /**
     * Returns the key-value properties associated with this element.
     *
     * @return the attribute map
     */
    AttributeMap attributes();

    /**
     * Returns the primitive, unique identifier for this element.
     *
     * @return the element ID
     */
    int id();
}
