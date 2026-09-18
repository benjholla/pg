package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * An empty, immutable implementation of {@link ElementSet}.
 * <p>
 * <b>What it represents:</b> A singleton representing a mathematical empty set of elements.
 * <p>
 * <b>Why it exists:</b> To prevent unnecessary memory allocations when returning empty results from graph queries.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return {@code empty()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Returning an empty set when a query yields no elements.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe as it is an empty, immutable singleton.
 * <p>
 * <b>Performance characteristics:</b> Zero allocation overhead, O(1) for all operations.
 */
public abstract class AbstractImmutableEmptyElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {

    @Override
    @SuppressWarnings("unchecked")
    public S materialize() {
        return (S) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S toImmutable() {
        return (S) this;
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Optional<T> one() {
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public S intersect(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return (S) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S difference(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return (S) this;
    }

    protected abstract S create(Collection<? extends T> elements);

    @Override
    @SuppressWarnings("unchecked")
    public S union(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return (S) this;
        }
        if (other instanceof ElementSet) {
            return (S) ((ElementSet<T>) other).toImmutable();
        }
        return create(other);
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
