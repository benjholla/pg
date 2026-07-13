package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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
        Set<Node> materialized = stream().collect(java.util.stream.Collectors.toUnmodifiableSet());
        return materialized.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(materialized);
    }
    /**
     * Returns a new immutable NodeSet snapshot containing elements present in both this set and the specified collection.
     */
    NodeSet intersect(Collection<? extends Node> other);

    /**
     * Returns a new immutable NodeSet snapshot containing elements from this set, excluding those in the specified collection.
     */
    NodeSet difference(Collection<? extends Node> other);

    /**
     * Returns a new immutable NodeSet snapshot containing all elements from this set and the specified collection.
     */
    NodeSet union(Collection<? extends Node> other);

    /**
     * Returns a standard set of the primitive integer IDs of the elements in this set.
     */
    Set<Integer> ids();

    /**
     * Returns an array of the primitive integer IDs of the elements in this set.
     */
    int[] toIdArray();
}
