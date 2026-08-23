package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A deferred evaluation pipeline for graph elements.
 *
 * @param <T> the type of elements in this set
 * @param <S> the concrete ElementSet type (NodeSet or EdgeSet) returned by this implementation
 */
public abstract class AbstractDeferredElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {

    protected final S source;
    protected final Predicate<T> combinedPredicate;

    protected AbstractDeferredElementSet(S source, Predicate<T> initialPredicate) {
        this.source = source;
        this.combinedPredicate = initialPredicate;
    }

    protected abstract S createDeferred(S source, Predicate<T> predicate);
    protected abstract S emptySet();
    protected abstract S genericImmutableSet(Collection<? extends T> elements);

    @Override
    public S withAttribute(String attribute) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> e.attributes().containsKey(attribute))
        );
    }

    @Override
    public S withAttribute(String attribute, AttributeValue... values) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(e -> {
                AttributeValue val = e.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) { return false; }
                return java.util.Arrays.asList(values).contains(val);
            })
        );
    }

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

    @Override
    public S materialize() {
        Set<T> materialized = new java.util.HashSet<>();
        for (T e : this) {
            materialized.add(e);
        }
        return materialized.isEmpty() ? emptySet() : genericImmutableSet(java.util.Collections.unmodifiableSet(materialized));
    }

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
    public S union(Collection<? extends T> other) {
                @SuppressWarnings("unchecked")
        S unioned = (S) materialize().union(other);
        return unioned;
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

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (!(o instanceof GraphElement)) { return false; }
        // Type check must be handled by caller or we just cast and catch ClassCastException,
        // but due to type erasure we can't do exact instance of T.
        // Wait, DeferredNodeSet does:
        // if (!(o instanceof Node)) { return false; }
        // Node e = (Node) o;
        // source.contains(e) ...
        // We will delegate to source.contains(o) first, which handles the exact type check safely.
        if (!source.contains(o)) {
            return false;
        }
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
