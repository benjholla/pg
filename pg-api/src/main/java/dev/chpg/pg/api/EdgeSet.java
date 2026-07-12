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
    EdgeSet filter(String attribute);
    EdgeSet filter(String attribute, AttributeValue... values);
    EdgeSet taggedWithAny(String... tags);
    EdgeSet taggedWithAll(String... tags);

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
