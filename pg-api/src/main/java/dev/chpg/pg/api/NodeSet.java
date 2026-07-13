package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface NodeSet extends Set<Node> {

    NodeSet EMPTY = new ImmutableEmptyNodeSet();

    static NodeSet empty() {
        return EMPTY;
    }

    NodeSet toImmutable();

    Optional<Node> one();
    /**
     * Filters this set to include only nodes with the specified attribute.
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines (e.g., {@code graph.nodes().taggedWithAny("A").attributedWith("B")}). It also allows
     * high-performance implementations to override these methods and exploit internal indices
     * (like bitmaps) for O(1) or O(log N) set intersections without full iteration.
     */
    default NodeSet attributedWith(String attribute) {
        Set<Node> filtered = stream()
            .filter(n -> n.attributes().containsKey(attribute))
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
    }
    /**
     * Filters this set to include only nodes with the specified attribute and value(s).
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines. It also allows high-performance implementations to override these methods and
     * exploit internal indices for faster set intersections without full iteration.
     */
    default NodeSet attributedWith(String attribute, AttributeValue... values) {
        Set<Node> filtered = stream()
            .filter(n -> {
                AttributeValue val = n.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) return false;
                for (AttributeValue v : values) {
                    if (Objects.equals(val, v)) return true;
                }
                return false;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
    }
    /**
     * Filters this set to include only nodes tagged with any of the specified tags.
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines. It also allows high-performance implementations to override these methods and
     * exploit internal indices (like bitmaps) for faster set intersections without full iteration.
     */
    default NodeSet taggedWithAny(String... tags) {
        Set<Node> filtered = stream()
            .filter(n -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (n.tags().contains(tag)) return true;
                }
                return false;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
    }
    /**
     * Filters this set to include only nodes tagged with all of the specified tags.
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines. It also allows high-performance implementations to override these methods and
     * exploit internal indices (like bitmaps) for faster set intersections without full iteration.
     */
    default NodeSet taggedWithAll(String... tags) {
        Set<Node> filtered = stream()
            .filter(n -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (!n.tags().contains(tag)) return false;
                }
                return true;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
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
