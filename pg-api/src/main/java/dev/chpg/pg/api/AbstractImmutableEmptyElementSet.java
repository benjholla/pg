package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract base class for empty, immutable implementations of element sets.
 *
 * @param <T> the type of the element
 * @param <S> the type of the element set
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
