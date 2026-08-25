package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * A generic, immutable implementation of {@link ElementSet}.
 * <p>
 * <b>What it represents:</b> An unmodifiable, materialized collection of elements.
 * <p>
 * <b>Why it exists:</b> To provide a guaranteed safe snapshot of elements that cannot be altered, ensuring query results remain stable even if the underlying graph mutates.
 * <p>
 * <b>Thread safety:</b> Fully thread-safe for reading because the internal state is fundamentally unmodifiable.
 * <p>
 * <b>Performance characteristics:</b> Requires O(N) memory allocation to materialize the underlying objects, but provides fast O(1) size checks and O(1) containment checks.
 *
 * @param <T> the type of graph element
 * @param <S> the type of element set
 */
public abstract class AbstractGenericImmutableElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {

    protected final Set<T> elements;

    /**
     * Constructs a new generic immutable element set from the provided elements.
     *
     * @param elements the collection of elements
     */
    public AbstractGenericImmutableElementSet(Collection<? extends T> elements) {
        this.elements = Set.copyOf(elements);
    }

    protected abstract S createImmutable(Collection<? extends T> elements);
    protected abstract S emptySet();

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

    @Override
    public S intersect(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return emptySet();
        }
        Set<T> intersected = new HashSet<>();
        for (T element : elements) {
            if (other.contains(element)) {
                intersected.add(element);
            }
        }
        return intersected.isEmpty() ? emptySet() : createImmutable(intersected);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S difference(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return (S) this;
        }
        Set<T> differenced = new HashSet<>();
        for (T element : elements) {
            if (!other.contains(element)) {
                differenced.add(element);
            }
        }
        return differenced.isEmpty() ? emptySet() : createImmutable(differenced);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S union(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return (S) this;
        }
        Set<T> unioned = new HashSet<>((int) ((elements.size() + other.size()) / 0.75f) + 1);
        unioned.addAll(elements);
        unioned.addAll(other);
        return createImmutable(unioned);
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new HashSet<>((int) (elements.size() / 0.75f) + 1);
        for (T element : elements) {
            ids.add(element.id());
        }
        return Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[elements.size()];
        int i = 0;
        for (T element : elements) {
            result[i++] = element.id();
        }
        return result;
    }
}
