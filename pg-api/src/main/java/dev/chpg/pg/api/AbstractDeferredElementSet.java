package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A deferred evaluation pipeline for graph elements.
 * <p>
 * <b>What it represents:</b> A lazily evaluated wrapper around an existing {@link ElementSet} that applies filtering predicates.
 * <p>
 * <b>Why it exists:</b> To prevent functional query pipelines from prematurely allocating memory when chained together. Filters are compounded and only execute when a terminal operation is called.
 * <p>
 * <b>Thread safety:</b> Relies on the thread safety of the underlying {@code ElementSet}. If the underlying graph changes during evaluation, a {@code ConcurrentModificationException} may occur.
 * <p>
 * <b>Performance characteristics:</b> Defers computation (O(1) creation). Terminal operations require O(N) evaluation time over the original set.
 *
 * @param <T> the type of graph element
 * @param <S> the type of element set
 */
public abstract class AbstractDeferredElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {
    protected final S source;
    protected final Predicate<T> combinedPredicate;

    /**
     * Constructs a new deferred element set.
     *
     * @param source           the underlying source element set
     * @param initialPredicate the initial filtering predicate
     */
    public AbstractDeferredElementSet(S source, Predicate<T> initialPredicate) {
        this.source = source;
        this.combinedPredicate = initialPredicate;
    }

    protected abstract S createDeferred(S source, Predicate<T> predicate);

    @Override
    @SuppressWarnings("unchecked")
    public S toImmutable() {
        return (S) materialize();
    }

    @Override
    public java.util.Optional<T> one() {
        Iterator<T> it = iterator();
        if (it.hasNext()) { return java.util.Optional.of(it.next()); }
        return java.util.Optional.empty();
    }

    @Override
    public S intersect(Collection<? extends T> other) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(other::contains)
        );
    }

    @Override
    public S difference(Collection<? extends T> other) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> !other.contains(e))
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public S union(Collection<? extends T> other) {
        return (S) ((S) materialize()).union(other);
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new java.util.HashSet<>((int) (size() / 0.75f) + 1);
        for (T element : this) {
            ids.add(element.id());
        }
        return java.util.Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[size()];
        int i = 0;
        for (T element : this) {
            result[i++] = element.id();
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (!(o instanceof GraphElement)) { return false; }
        try {
            T e = (T) o;
            return source.contains(e) && combinedPredicate.test(e);
        } catch (ClassCastException ex) {
            return false;
        }
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> sourceIterator = source.iterator();

        return new Iterator<T>() {
            private T nextElement = null;

            private void advance() {
                while (nextElement == null && sourceIterator.hasNext()) {
                    T candidate = sourceIterator.next();
                    if (combinedPredicate.test(candidate)) {
                        nextElement = candidate;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (nextElement == null) { advance(); }
                return nextElement != null;
            }

            @Override
            public T next() {
                if (!hasNext()) { throw new NoSuchElementException(); }
                T result = nextElement;
                nextElement = null;
                return result;
            }
        };
    }

    @Override
    public boolean isSizeKnown() {
        return false;
    }

    @Override
    public boolean isMaterialized() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Computes the size of this deferred set by evaluating the pipeline.
     * <p>
     * <b>Warning:</b> This is an O(N) operation that iterates the source and tests the predicate.
     * Do not use in a loop condition (e.g. {@code for (int i = 0; i < set.size(); i++)}).
     */
    @Override
    public int size() {
        int count = 0;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}
