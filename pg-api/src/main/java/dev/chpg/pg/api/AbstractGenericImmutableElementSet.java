package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * A generic, immutable implementation of an {@link ElementSet}.
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
 *
 * @param <T> the type of element, typically {@link Node} or {@link Edge}
 */
public abstract class AbstractGenericImmutableElementSet<T extends GraphElement> extends AbstractSet<T> implements ElementSet<T> {

    protected final Set<T> elements;

    /**
     * Constructs a new generic immutable element set from the provided elements.
     *
     * @param elements the collection of elements
     */
    protected AbstractGenericImmutableElementSet(Collection<? extends T> elements) {
        this.elements = Set.copyOf(elements);
    }

    /**
     * Wraps a collection into a specific ElementSet implementation.
     *
     * @param elements the elements to wrap
     * @return the ElementSet
     */
    protected abstract ElementSet<T> wrap(Collection<? extends T> elements);

    /**
     * Returns an empty set of the appropriate type.
     *
     * @return an empty ElementSet
     */
    protected abstract ElementSet<T> empty();

    @Override
    public ElementSet<T> toImmutable() {
        return this;
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
    public ElementSet<T> intersect(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return empty();
        }
        Set<T> intersected = new HashSet<>();
        for (T n : elements) {
            if (other.contains(n)) {
                intersected.add(n);
            }
        }
        return intersected.isEmpty() ? empty() : wrap(Collections.unmodifiableSet(intersected));
    }

    @Override
    public ElementSet<T> difference(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<T> differenced = new HashSet<>();
        for (T n : elements) {
            if (!other.contains(n)) {
                differenced.add(n);
            }
        }
        return differenced.isEmpty() ? empty() : wrap(Collections.unmodifiableSet(differenced));
    }

    @Override
    public ElementSet<T> union(Collection<? extends T> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<T> unioned = new HashSet<>((int) ((elements.size() + other.size()) / 0.75f) + 1);
        unioned.addAll(elements);
        unioned.addAll(other);
        return wrap(Collections.unmodifiableSet(unioned));
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
