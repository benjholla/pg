package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * A specialized set for managing collections of {@link Node}s.
 * <p>
 * <b>What it represents:</b> A distinct collection of graph nodes, supporting set-theoretic math and fluent filtering.
 * <p>
 * <b>Why it exists:</b> To provide a zero-allocation, functional query pipeline for graph analysis, enabling operations like filtering without intermediate collection allocation.
 * <p>
 * <b>When to use it:</b> Use {@code NodeSet} whenever dealing with aggregate collections of nodes, especially when extracting properties or executing declarative filters.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Filtering nodes via fluent predicates (e.g., {@code nodes.withAnyTag("Person")}).</li>
 * <li>Extracting primitive ID arrays for bulk operations (e.g., {@code nodes.toIdArray()}).</li>
 * <li>Forcing eager materialization of deferred pipelines via {@code materialize()}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> {@code NodeSet} implementations may be "live" (dynamically reflecting changes in the underlying graph) or "snapshot" based. Many filtering methods return a deferred evaluation pipeline that only executes when terminal operations (like {@code size()} or {@code toIdArray()}) are invoked.
 * <p>
 * <b>Thread safety:</b> Thread safety guarantees depend heavily on the concrete implementation. Modifying the underlying graph while iterating a live NodeSet will likely produce a {@code ConcurrentModificationException}.
 * <p>
 * <b>Performance characteristics:</b> The fluent filtering API (e.g., {@code withAttribute}) returns a deferred, zero-allocation wrapper. Terminal operations on deferred sets evaluate the pipeline and take O(N) time.
 */
public interface NodeSet extends Set<Node> {

    /**
     * An immutable, empty node set singleton.
     */
    NodeSet EMPTY = new ImmutableEmptyNodeSet();

    /**
     * Returns an empty node set.
     *
     * @return an empty NodeSet
     */
    static NodeSet empty() {
        return EMPTY;
    }

    /**
     * Converts this node set into an immutable snapshot.
     *
     * @return an immutable NodeSet
     */
    NodeSet toImmutable();

    /**
     * Returns any single node from this set, if it is not empty.
     *
     * @return an Optional containing a node, or empty if the set is empty
     */
    Optional<Node> one();

    /**
     * Filters this set to include only nodes with the specified attribute key.
     *
     * @param attribute the attribute key to check for
     * @return a deferred NodeSet containing matching nodes
     */
    default NodeSet withAttribute(String attribute) {
        return new DeferredNodeSet(this, n -> n.attributes().containsKey(attribute));
    }

    /**
     * Filters this set to include only nodes with the specified attribute key matching any of the given values.
     *
     * @param attribute the attribute key to check for
     * @param values    the allowed attribute values
     * @return a deferred NodeSet containing matching nodes
     */
    default NodeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredNodeSet(this, n -> {
            AttributeValue val = n.attributes().get(attribute);
            if (val == null || values == null || values.length == 0) { return false; }
            for (AttributeValue v : values) {
                if (java.util.Objects.equals(val, v)) { return true; }
            }
            return false;
        });
    }

    /**
     * Filters this set to include only nodes possessing at least one of the specified tags.
     *
     * @param tags the tags to check for
     * @return a deferred NodeSet containing matching nodes
     */
    default NodeSet withAnyTag(String... tags) {
        return new DeferredNodeSet(this, n -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (n.tags().contains(tag)) { return true; }
            }
            return false;
        });
    }

    /**
     * Filters this set to include only nodes possessing all of the specified tags.
     *
     * @param tags the tags to check for
     * @return a deferred NodeSet containing matching nodes
     */
    default NodeSet withAllTags(String... tags) {
        return new DeferredNodeSet(this, n -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (!n.tags().contains(tag)) { return false; }
            }
            return true;
        });
    }


    /**
     * Forces eager evaluation of the deferred pipeline, materializing
     * the final IDs into a high-performance array in memory.
     * Note: This incurs an allocation and iteration cost.
     *
     * @return a materialized, immutable NodeSet
     */
    default NodeSet materialize() {
        Set<Node> materialized = new java.util.HashSet<>();
        for (Node n : this) {
            materialized.add(n);
        }
        return materialized.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(java.util.Collections.unmodifiableSet(materialized));
    }
    /**
     * Returns a new immutable NodeSet snapshot containing elements present in both this set and the specified collection.
     * @param other the collection to perform the set operation with
     * @return the intersected NodeSet
     */
    NodeSet intersect(Collection<? extends Node> other);

    /**
     * Returns a new immutable NodeSet snapshot containing elements from this set, excluding those in the specified collection.
     * @param other the collection to perform the set operation with
     * @return the differenced NodeSet
     */
    NodeSet difference(Collection<? extends Node> other);

    /**
     * Returns a new immutable NodeSet snapshot containing all elements from this set and the specified collection.
     * @param other the collection to perform the set operation with
     * @return the unioned NodeSet
     */
    NodeSet union(Collection<? extends Node> other);

    /**
     * Returns true if this set is already backed by a flat, allocated
     * memory structure. Returns false if this set requires computation
     * (lazy evaluation) during iteration.
     *
     * @return true if materialized, false otherwise
     */
    boolean isMaterialized();

    /**
     * Returns true if the size of the set can be determined in O(1) time
     * without iterating or evaluating the elements.
     *
     * @return true if the size is known in O(1) time, false otherwise
     */
    default boolean isSizeKnown() {
        return true;
    }

    /**
     * Returns a standard set of the primitive integer IDs of the elements in this set.
     *
     * @return a set of primitive integer IDs
     */
    Set<Integer> ids();

    /**
     * Returns an array of the primitive integer IDs of the elements in this set.
     *
     * @return an array of primitive integer IDs
     */
    int[] toIdArray();
}
