package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

public abstract class AbstractDeferredElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {
    protected final S source;
    protected final Predicate<T> combinedPredicate;

    public AbstractDeferredElementSet(S source, Predicate<T> initialPredicate) {
        this.source = source;
        this.combinedPredicate = initialPredicate;
    }

    protected abstract S createDeferred(S source, Predicate<T> predicate);

    @SuppressWarnings("unchecked")
    @Override
    public S withAttribute(String attribute) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(element -> element.attributes().containsKey(attribute))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAttribute(String attribute, AttributeValue... values) {
        return createDeferred(
            this.source,
            this.combinedPredicate.and(element -> {
                AttributeValue val = element.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) { return false; }
                return java.util.Arrays.asList(values).contains(val);
            })
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public S withAnyTag(String... tags) {
        return createDeferred(
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

    @SuppressWarnings("unchecked")
    @Override
    public S withAllTags(String... tags) {
        return createDeferred(
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
        return combinedPredicate.test((T) o);
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
