package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * An empty, immutable abstract base implementation of {@link ElementSet}.
 *
 * @param <T> the type of elements in this set
 * @param <S> the concrete ElementSet type (NodeSet or EdgeSet) returned by this implementation
 */
public abstract class AbstractImmutableEmptyElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {

    protected abstract S emptySet();
    protected abstract S genericImmutableSet(Collection<? extends T> elements);

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
    public boolean isSizeKnown() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
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
        return genericImmutableSet(other);
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
