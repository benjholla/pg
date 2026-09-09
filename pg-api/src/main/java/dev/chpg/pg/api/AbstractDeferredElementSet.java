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
 * <b>When to use it:</b> Primarily used internally by the API when operations like {@code withAttribute(String)} are invoked.
 * <p>
 * <b>Thread safety:</b> Relies on the thread safety of the underlying set.
 *
 * @param <T> the type of graph element
 * @param <S> the concrete type of the element set
 */
public abstract class AbstractDeferredElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {
    protected final S source;
    protected final Predicate<T> combinedPredicate;

    protected AbstractDeferredElementSet(S source, Predicate<T> combinedPredicate) {
        this.source = source;
        this.combinedPredicate = combinedPredicate;
    }

    protected abstract S createDeferred(S source, Predicate<T> predicate);
    protected abstract S emptySet();

    @SuppressWarnings("unchecked")
    @Override
    public S withAttribute(String attribute) {
        return createDeferred(source, combinedPredicate.and(e -> e.attributes().containsKey(attribute)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAttribute(String attribute, AttributeValue... values) {
        return createDeferred(source, combinedPredicate.and(e -> {
            AttributeValue val = e.attributes().get(attribute);
            if (val == null || values == null || values.length == 0) { return false; }
            return java.util.Arrays.asList(values).contains(val);
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAnyTag(String... tags) {
        return createDeferred(source, combinedPredicate.and(e -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (e.tags().contains(tag)) { return true; }
            }
            return false;
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAllTags(String... tags) {
        return createDeferred(source, combinedPredicate.and(e -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (!e.tags().contains(tag)) { return false; }
            }
            return true;
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S toImmutable() {
        return (S) materialize();
    }

    @Override
    public java.util.Optional<T> one() {
        Iterator<T> it = iterator();
        if (it.hasNext()) { return java.util.Optional.of(it.next()); }
        return java.util.Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public S intersect(Collection<? extends T> other) {
        return createDeferred(source, combinedPredicate.and(other::contains));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S difference(Collection<? extends T> other) {
        return createDeferred(source, combinedPredicate.and(e -> !other.contains(e)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S union(Collection<? extends T> other) {
        return (S) materialize().union(other);
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new java.util.HashSet<>((int) (size() / 0.75f) + 1);
        for (T e : this) {
            ids.add(e.id());
        }
        return java.util.Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[size()];
        int i = 0;
        for (T e : this) {
            result[i++] = e.id();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        if (!(o instanceof GraphElement)) { return false; }
        T e = (T) o;
        return source.contains(e) && combinedPredicate.test(e);
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
