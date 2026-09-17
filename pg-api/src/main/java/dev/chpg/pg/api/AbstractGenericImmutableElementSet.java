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
 * <b>When to use it:</b> Primarily used internally to return materialized results from operations like {@link ElementSet#materialize()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Caching stable query results for repeated analysis.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe for reading because the internal state is fundamentally unmodifiable.
 * <p>
 * <b>Performance characteristics:</b> Requires O(N) memory allocation to materialize the underlying objects, but provides fast O(1) size checks and O(1) containment checks.
 */
public abstract class AbstractGenericImmutableElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {

    protected final Set<T> elements;

    public AbstractGenericImmutableElementSet(Collection<? extends T> elements) {
        this.elements = Set.copyOf(elements);
    }

    protected abstract S empty();
    protected abstract S createImmutableSet(Set<T> elements);

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
            return empty();
        }
        Set<T> intersected = new HashSet<>();
        for (T e : elements) {
            if (other.contains(e)) {
                intersected.add(e);
            }
        }
        return intersected.isEmpty() ? empty() : createImmutableSet(Collections.unmodifiableSet(intersected));
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
        return differenced.isEmpty() ? empty() : createImmutableSet(Collections.unmodifiableSet(differenced));
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
        return createImmutableSet(Collections.unmodifiableSet(unioned));
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
