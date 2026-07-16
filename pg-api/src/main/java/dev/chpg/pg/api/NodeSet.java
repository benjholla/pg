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

    NodeSet EMPTY = new ImmutableEmptyNodeSet();

    static NodeSet empty() {
        return EMPTY;
    }

    NodeSet toImmutable();

    Optional<Node> one();
    default NodeSet withAttribute(String attribute) {
        return new DeferredNodeSet(this, n -> n.attributes().containsKey(attribute));
    }
    default NodeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredNodeSet(this, n -> {
            AttributeValue val = n.attributes().get(attribute);
            if (val == null || values == null || values.length == 0) return false;
            for (AttributeValue v : values) {
                if (java.util.Objects.equals(val, v)) return true;
            }
            return false;
        });
    }
    default NodeSet withAnyTag(String... tags) {
        return new DeferredNodeSet(this, n -> {
            if (tags == null || tags.length == 0) return false;
            for (String tag : tags) {
                if (n.tags().contains(tag)) return true;
            }
            return false;
        });
    }
    default NodeSet withAllTags(String... tags) {
        return new DeferredNodeSet(this, n -> {
            if (tags == null || tags.length == 0) return false;
            for (String tag : tags) {
                if (!n.tags().contains(tag)) return false;
            }
            return true;
        });
    }


    /**
     * Forces eager evaluation of the deferred pipeline, materializing
     * the final IDs into a high-performance array in memory.
     * Note: This incurs an allocation and iteration cost.
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
     */
    NodeSet intersect(Collection<? extends Node> other);

    /**
     * Returns a new immutable NodeSet snapshot containing elements from this set, excluding those in the specified collection.
     * @param other the collection to perform the set operation with
     */
    NodeSet difference(Collection<? extends Node> other);

    /**
     * Returns a new immutable NodeSet snapshot containing all elements from this set and the specified collection.
     * @param other the collection to perform the set operation with
     */
    NodeSet union(Collection<? extends Node> other);

    /**
     * Returns true if this set is already backed by a flat, allocated
     * memory structure. Returns false if this set requires computation
     * (lazy evaluation) during iteration.
     */
    boolean isMaterialized();

    /**
     * Returns a standard set of the primitive integer IDs of the elements in this set.
     */
    Set<Integer> ids();

    /**
     * Returns an array of the primitive integer IDs of the elements in this set.
     */
    int[] toIdArray();
}
