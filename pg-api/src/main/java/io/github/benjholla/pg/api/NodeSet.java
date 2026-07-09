package io.github.benjholla.pg.api;

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
    NodeSet filter(String attribute);
    NodeSet filter(String attribute, AttributeValue... values);

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
