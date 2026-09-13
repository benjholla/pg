package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An abstract base class for deferred evaluation pipelines of graph elements.
 *
 * @param <T> the type of the element
 * @param <S> the type of the element set
 */
public abstract class AbstractDeferredElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {
    protected final S source;
    protected final Predicate<T> combinedPredicate;

    protected AbstractDeferredElementSet(S source, Predicate<T> combinedPredicate) {
        this.source = source;
        this.combinedPredicate = combinedPredicate;
    }

    protected abstract S createDeferred(S source, Predicate<T> predicate);
    protected abstract S createImmutable(Set<T> materialized);
    protected abstract S emptySet();

    @SuppressWarnings("unchecked")
    @Override
    public S withAttribute(String attribute) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> e.attributes().containsKey(attribute))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAttribute(String attribute, AttributeValue... values) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> {
                AttributeValue val = e.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) { return false; }
                for (AttributeValue v : values) {
                    if (java.util.Objects.equals(val, v)) { return true; }
                }
                return false;
            })
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAnyTag(String... tags) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> {
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (e.tags().contains(tag)) { return true; }
                }
                return false;
            })
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAllTags(String... tags) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> {
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (!e.tags().contains(tag)) { return false; }
                }
                return true;
            })
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S materialize() {
        Set<T> materialized = new java.util.HashSet<>();
        for (T e : this) {
            materialized.add(e);
        }
        return materialized.isEmpty() ? emptySet() : createImmutable(java.util.Collections.unmodifiableSet(materialized));
    }

    @SuppressWarnings("unchecked")
    @Override
    public S toImmutable() {
        return materialize();
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
        return createDeferred(
            this.source,
            this.combinedPredicate.and(other::contains)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S difference(Collection<? extends T> other) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> !other.contains(e))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S union(Collection<? extends T> other) {
        return (S) materialize().union(other);
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

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        if (!(o instanceof GraphElement)) { return false; }
        if (!source.contains(o)) { return false; }
        T e = (T) o;
        return combinedPredicate.test(e);
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
