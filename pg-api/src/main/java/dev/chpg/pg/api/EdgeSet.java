package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * A specialized set for managing collections of {@link Edge}s.
 * <p>
 * <b>What it represents:</b> A distinct collection of graph edges, supporting set-theoretic math and fluent filtering.
 * <p>
 * <b>Why it exists:</b> To provide a zero-allocation, functional query pipeline for graph analysis, enabling operations like filtering without intermediate collection allocation.
 * <p>
 * <b>When to use it:</b> Use {@code EdgeSet} whenever dealing with aggregate collections of edges, especially when extracting properties or executing declarative filters.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Filtering edges via fluent predicates (e.g., {@code edges.withAnyTag("knows")}).</li>
 * <li>Extracting primitive ID arrays for bulk operations (e.g., {@code edges.toIdArray()}).</li>
 * <li>Forcing eager materialization of deferred pipelines via {@code materialize()}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> {@code EdgeSet} implementations may be "live" (dynamically reflecting changes in the underlying graph) or "snapshot" based. Many filtering methods return a deferred evaluation pipeline that only executes when terminal operations (like {@code size()} or {@code toIdArray()}) are invoked.
 * <p>
 * <b>Thread safety:</b> Thread safety guarantees depend heavily on the concrete implementation. Modifying the underlying graph while iterating a live EdgeSet will likely produce a {@code ConcurrentModificationException}.
 * <p>
 * <b>Performance characteristics:</b> The fluent filtering API (e.g., {@code withAttribute}) returns a deferred, zero-allocation wrapper. Terminal operations on deferred sets evaluate the pipeline and take O(N) time.
 */
public interface EdgeSet extends Set<Edge> {

    EdgeSet EMPTY = new ImmutableEmptyEdgeSet();

    static EdgeSet empty() {
        return EMPTY;
    }

    EdgeSet toImmutable();

    Optional<Edge> one();
    default EdgeSet withAttribute(String attribute) {
        return new DeferredEdgeSet(this, e -> e.attributes().containsKey(attribute));
    }
    default EdgeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredEdgeSet(this, e -> {
            AttributeValue val = e.attributes().get(attribute);
            if (val == null || values == null || values.length == 0) return false;
            for (AttributeValue v : values) {
                if (java.util.Objects.equals(val, v)) return true;
            }
            return false;
        });
    }
    default EdgeSet withAnyTag(String... tags) {
        return new DeferredEdgeSet(this, e -> {
            if (tags == null || tags.length == 0) return false;
            for (String tag : tags) {
                if (e.tags().contains(tag)) return true;
            }
            return false;
        });
    }
    default EdgeSet withAllTags(String... tags) {
        return new DeferredEdgeSet(this, e -> {
            if (tags == null || tags.length == 0) return false;
            for (String tag : tags) {
                if (!e.tags().contains(tag)) return false;
            }
            return true;
        });
    }


    /**
     * Forces eager evaluation of the deferred pipeline, materializing
     * the final IDs into a high-performance array in memory.
     * Note: This incurs an allocation and iteration cost.
     */
    default EdgeSet materialize() {
        Set<Edge> materialized = stream().collect(java.util.stream.Collectors.toUnmodifiableSet());
        return materialized.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(materialized);
    }
    /**
     * Returns a new immutable EdgeSet snapshot containing elements present in both this set and the specified collection.
     * @param other the collection to perform the set operation with
     */
    EdgeSet intersect(Collection<? extends Edge> other);

    /**
     * Returns a new immutable EdgeSet snapshot containing elements from this set, excluding those in the specified collection.
     * @param other the collection to perform the set operation with
     */
    EdgeSet difference(Collection<? extends Edge> other);

    /**
     * Returns a new immutable EdgeSet snapshot containing all elements from this set and the specified collection.
     * @param other the collection to perform the set operation with
     */
    EdgeSet union(Collection<? extends Edge> other);

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
