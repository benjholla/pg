package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract base class for generic, immutable implementations of element sets.
 *
 * @param <T> the type of the element
 * @param <S> the type of the element set
 */
public abstract class AbstractGenericImmutableElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {
    protected final Set<T> elements;

    protected AbstractGenericImmutableElementSet(Collection<? extends T> elements) {
        this.elements = Set.copyOf(elements);
    }

    protected abstract S emptySet();
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
        return elements.size();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public Optional<T> one() {
        if (elements.isEmpty()) { return Optional.empty(); }
        return Optional.of(elements.iterator().next());
    }

    @SuppressWarnings("unchecked")
    @Override
    public S intersect(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return emptySet();
        }
        Set<T> intersected = new HashSet<>();
        for (T e : elements) {
            if (other.contains(e)) {
                intersected.add(e);
            }
        }
        return intersected.isEmpty() ? emptySet() : createImmutable(Collections.unmodifiableSet(intersected));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S difference(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return (S) this;
        }
        Set<T> differenced = new HashSet<>();
        for (T e : elements) {
            if (!other.contains(e)) {
                differenced.add(e);
            }
        }
        return differenced.isEmpty() ? emptySet() : createImmutable(Collections.unmodifiableSet(differenced));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S union(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return (S) this;
        }
        Set<T> unioned = new HashSet<>((int) ((elements.size() + other.size()) / 0.75f) + 1);
        unioned.addAll(elements);
        unioned.addAll(other);
        return createImmutable(Collections.unmodifiableSet(unioned));
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new HashSet<>((int) (elements.size() / 0.75f) + 1);
        for (T e : elements) {
            ids.add(e.id());
        }
        return Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[elements.size()];
        int i = 0;
        for (T e : elements) {
            result[i++] = e.id();
        }
        return result;
    }
}
