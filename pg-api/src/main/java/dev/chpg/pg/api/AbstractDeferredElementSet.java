package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A deferred evaluation pipeline for graph elements.
 * <p>
 * <b>What it represents:</b> A lazily evaluated wrapper around an existing {@link ElementSet} that applies filtering predicates.
 * <p>
 * <b>Why it exists:</b> To prevent functional query pipelines from prematurely allocating memory when chained together. Filters are compounded and only execute when a terminal operation is called. This abstract class eliminates duplicated logic between {@link DeferredNodeSet} and {@link DeferredEdgeSet}.
 * <p>
 * <b>Performance characteristics:</b> Defers computation (O(1) creation). Terminal operations require O(N) evaluation time over the original set.
 *
 * @param <T> the type of elements maintained by this set
 * @param <S> the specific type of ElementSet this defers to (NodeSet or EdgeSet)
 */
public abstract class AbstractDeferredElementSet<T extends GraphElement, S extends ElementSet<T>> extends AbstractSet<T> implements ElementSet<T> {

    /**
     * The underlying source element set.
     */
    protected final S source;

    /**
     * The combined filtering predicate.
     */
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

    /**
     * Creates a new deferred set of the specific subtype.
     *
     * @param source    the source element set
     * @param predicate the combined predicate
     * @return a new deferred set
     */
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
                return Arrays.asList(values).contains(val);
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
    public Optional<T> one() {
        Iterator<T> it = iterator();
        if (it.hasNext()) { return Optional.of(it.next()); }
        return Optional.empty();
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
        Set<Integer> ids = new HashSet<>((int) (size() / 0.75f) + 1);
        for (T element : this) {
            ids.add(element.id());
        }
        return Collections.unmodifiableSet(ids);
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
        return source.contains(o) && combinedPredicate.test((T) o);
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
