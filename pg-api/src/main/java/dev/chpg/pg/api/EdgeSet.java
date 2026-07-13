package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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
     */
    EdgeSet intersect(Collection<? extends Edge> other);

    /**
     * Returns a new immutable EdgeSet snapshot containing elements from this set, excluding those in the specified collection.
     */
    EdgeSet difference(Collection<? extends Edge> other);

    /**
     * Returns a new immutable EdgeSet snapshot containing all elements from this set and the specified collection.
     */
    EdgeSet union(Collection<? extends Edge> other);

    /**
     * Returns a standard set of the primitive integer IDs of the elements in this set.
     */
    Set<Integer> ids();

    /**
     * Returns an array of the primitive integer IDs of the elements in this set.
     */
    int[] toIdArray();
}
