package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * An empty, immutable implementation of {@link NodeSet}.
 * <p>
 * <b>What it represents:</b> A singleton representing a mathematical empty set of nodes.
 * <p>
 * <b>Why it exists:</b> To prevent unnecessary memory allocations when returning empty results from graph queries.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return {@link NodeSet#empty()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Returning an empty set when a query yields no nodes.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe as it is an empty, immutable singleton.
 * <p>
 * <b>Performance characteristics:</b> Zero allocation overhead, O(1) for all operations.
 */
public final class ImmutableEmptyNodeSet extends AbstractSet<Node> implements NodeSet {

    @Override
    public NodeSet materialize() {
        return this;
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }
    @Override
public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Node> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Optional<Node> one() {
        return Optional.empty();
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return this;
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return this;
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        if (other instanceof NodeSet) {
            return ((NodeSet) other).toImmutable();
        }
        return new GenericImmutableNodeSet(other);
    }

    @Override
    public Set<Integer> ids() {
        return Collections.emptySet();
    }

    @Override
    public int[] toIdArray() {
        return new int[0];
    }
}
