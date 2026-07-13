package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface EdgeSet extends Set<Edge> {

    EdgeSet EMPTY = new ImmutableEmptyEdgeSet();

    static EdgeSet empty() {
        return EMPTY;
    }

    EdgeSet toImmutable();

    Optional<Edge> one();
    /**
     * Filters this set to include only edges with the specified attribute.
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines (e.g., {@code graph.nodes().taggedWithAny("A").attributedWith("B")}). It also allows
     * high-performance implementations to override these methods and exploit internal indices
     * (like bitmaps) for O(1) or O(log N) set intersections without full iteration.
     */
    default EdgeSet attributedWith(String attribute) {
        Set<Edge> filtered = stream()
            .filter(e -> e.attributes().containsKey(attribute))
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(filtered);
    }
    /**
     * Filters this set to include only edges with the specified attribute and value(s).
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines. It also allows high-performance implementations to override these methods and
     * exploit internal indices for faster set intersections without full iteration.
     */
    default EdgeSet attributedWith(String attribute, AttributeValue... values) {
        Set<Edge> filtered = stream()
            .filter(e -> {
                AttributeValue val = e.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) return false;
                for (AttributeValue v : values) {
                    if (Objects.equals(val, v)) return true;
                }
                return false;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(filtered);
    }
    /**
     * Filters this set to include only edges tagged with any of the specified tags.
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines. It also allows high-performance implementations to override these methods and
     * exploit internal indices (like bitmaps) for faster set intersections without full iteration.
     */
    default EdgeSet taggedWithAny(String... tags) {
        Set<Edge> filtered = stream()
            .filter(e -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (e.tags().contains(tag)) return true;
                }
                return false;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(filtered);
    }
    /**
     * Filters this set to include only edges tagged with all of the specified tags.
     * <p>
     * <b>Architectural Rationale:</b>
     * Anchoring filtering operations directly on the set (rather than the Graph) enables fluent query
     * pipelines. It also allows high-performance implementations to override these methods and
     * exploit internal indices (like bitmaps) for faster set intersections without full iteration.
     */
    default EdgeSet taggedWithAll(String... tags) {
        Set<Edge> filtered = stream()
            .filter(e -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (!e.tags().contains(tag)) return false;
                }
                return true;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(filtered);
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
