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
 * <b>When to use it:</b> Primarily used internally by the API when operations like {@link ElementSet#withAttribute(String)} are invoked.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Chaining multiple filters before calling {@code size()} or {@code toIdArray()}.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Relies on the thread safety of the underlying {@code ElementSet}. If the underlying graph changes during evaluation, a {@code ConcurrentModificationException} may occur.
 * <p>
 * <b>Performance characteristics:</b> Defers computation (O(1) creation). Terminal operations require O(N) evaluation time over the original set.
 *
 * @param <T> the type of element, typically {@link Node} or {@link Edge}
 */
public abstract class AbstractDeferredElementSet<T extends GraphElement> extends AbstractSet<T> implements ElementSet<T> {
    protected final ElementSet<T> source;
    protected final Predicate<T> combinedPredicate;

    /**
     * Constructs a new deferred element set.
     *
     * @param source           the underlying source element set
     * @param initialPredicate the initial filtering predicate
     */
    protected AbstractDeferredElementSet(ElementSet<T> source, Predicate<T> initialPredicate) {
        this.source = source;
        this.combinedPredicate = initialPredicate;
    }

    /**
     * Wraps the given source and predicate into a concrete deferred element set.
     *
     * @param source the source element set
     * @param predicate the predicate
     * @return the wrapped ElementSet
     */
    protected abstract ElementSet<T> wrap(ElementSet<T> source, Predicate<T> predicate);

    @Override
    public ElementSet<T> withAttribute(String attribute) {
        return wrap(
            this.source,
            this.combinedPredicate.and(element -> element.attributes().containsKey(attribute))
        );
    }

    @Override
    public ElementSet<T> withAttribute(String attribute, AttributeValue... values) {
        return wrap(
            this.source,
            this.combinedPredicate.and(element -> {
                AttributeValue val = element.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) { return false; }
                return java.util.Arrays.asList(values).contains(val);
            })
        );
    }

    @Override
    public ElementSet<T> withAnyTag(String... tags) {
        return wrap(
            this.source,
            this.combinedPredicate.and(element -> {
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (element.tags().contains(tag)) { return true; }
                }
                return false;
            })
        );
    }

    @Override
    public ElementSet<T> withAllTags(String... tags) {
        return wrap(
            this.source,
            this.combinedPredicate.and(element -> {
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (!element.tags().contains(tag)) { return false; }
                }
                return true;
            })
        );
    }

    @Override
    public ElementSet<T> toImmutable() {
        return materialize();
    }

    @Override
    public java.util.Optional<T> one() {
        Iterator<T> it = iterator();
        if (it.hasNext()) { return java.util.Optional.of(it.next()); }
        return java.util.Optional.empty();
    }

    @Override
    public ElementSet<T> intersect(Collection<? extends T> other) {
        return wrap(
            this.source,
            this.combinedPredicate.and(other::contains)
        );
    }

    @Override
    public ElementSet<T> difference(Collection<? extends T> other) {
        return wrap(
            this.source,
            this.combinedPredicate.and(e -> !other.contains(e))
        );
    }

    @Override
    public ElementSet<T> union(Collection<? extends T> other) {
        return materialize().union(other);
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
        // Type safety relies on caller - if generic T matches o it's fine, else it will return false eventually
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
