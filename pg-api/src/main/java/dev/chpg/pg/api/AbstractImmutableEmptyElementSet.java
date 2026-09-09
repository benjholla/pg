package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * An empty, immutable base implementation of {@link ElementSet}.
 * <p>
 * <b>What it represents:</b> A singleton representing a mathematical empty set of elements.
 * <p>
 * <b>Why it exists:</b> To prevent unnecessary memory allocations when returning empty results from graph queries.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return empty sets.
 * <p>
 * <b>Thread safety:</b> Fully thread-safe as it is an empty, immutable singleton.
 * <p>
 * <b>Performance characteristics:</b> Zero allocation overhead, O(1) for all operations.
 *
 * @param <T> the type of graph element
 * @param <S> the concrete type of the element set
 */
public abstract class AbstractImmutableEmptyElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {

    protected abstract S createImmutable(Collection<? extends T> elements);

    @SuppressWarnings("unchecked")
    @Override
    public S materialize() {
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @Override
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

    @SuppressWarnings("unchecked")
    @Override
    public S intersect(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S difference(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S union(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return (S) this;
        }
        if (other instanceof ElementSet) {
            return (S) ((ElementSet<T>) other).toImmutable();
        }
        return createImmutable(other);
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
