package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

public abstract class AbstractDeferredElementSet<T extends GraphElement> extends AbstractSet<T> implements ElementSet<T> {
    protected final ElementSet<T> source;
    protected final Predicate<T> combinedPredicate;

    public AbstractDeferredElementSet(ElementSet<T> source, Predicate<T> initialPredicate) {
        this.source = source;
        this.combinedPredicate = initialPredicate;
    }

    protected abstract ElementSet<T> createDeferredSet(ElementSet<T> source, Predicate<T> predicate);

    protected ElementSet<T> createMaterializedSet(Set<T> materialized) {
        return materializeSet(materialized);
    }

    protected abstract ElementSet<T> materializeSet(Set<T> materialized);

    @Override
    public ElementSet<T> withAttribute(String attribute) {
        return createDeferredSet(this.source, this.combinedPredicate.and(e -> e.attributes().containsKey(attribute)));
    }

    @Override
    public ElementSet<T> withAttribute(String attribute, AttributeValue... values) {
        return createDeferredSet(this.source, this.combinedPredicate.and(e -> {
            AttributeValue val = e.attributes().get(attribute);
            if (val == null || values == null || values.length == 0) return false;
            return java.util.Arrays.asList(values).contains(val);
        }));
    }

    @Override
    public ElementSet<T> withAnyTag(String... tags) {
        return createDeferredSet(this.source, this.combinedPredicate.and(e -> {
            if (tags == null || tags.length == 0) return false;
            for (String tag : tags) { if (e.tags().contains(tag)) return true; }
            return false;
        }));
    }

    @Override
    public ElementSet<T> withAllTags(String... tags) {
        return createDeferredSet(this.source, this.combinedPredicate.and(e -> {
            if (tags == null || tags.length == 0) return false;
            for (String tag : tags) { if (!e.tags().contains(tag)) return false; }
            return true;
        }));
    }

    @Override
    public ElementSet<T> materialize() {
        Set<T> materialized = new java.util.HashSet<>();
        for (T e : this) { materialized.add(e); }
        return createMaterializedSet(materialized);
    }

    @Override
    public ElementSet<T> toImmutable() { return materialize(); }

    @Override
    public java.util.Optional<T> one() {
        Iterator<T> it = iterator();
        return it.hasNext() ? java.util.Optional.of(it.next()) : java.util.Optional.empty();
    }

    @Override
    public ElementSet<T> intersect(Collection<? extends T> other) {
        return createDeferredSet(this.source, this.combinedPredicate.and(other::contains));
    }

    @Override
    public ElementSet<T> difference(Collection<? extends T> other) {
        return createDeferredSet(this.source, this.combinedPredicate.and(e -> !other.contains(e)));
    }

    @Override
    public ElementSet<T> union(Collection<? extends T> other) {
        return materialize().union(other);
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new java.util.HashSet<>((int) (size() / 0.75f) + 1);
        for (T element : this) { ids.add(element.id()); }
        return java.util.Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[size()];
        int i = 0;
        for (T element : this) { result[i++] = element.id(); }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
             T e = (T) o;
             return source.contains(e) && combinedPredicate.test(e);
        } catch(ClassCastException ex) { return false; }
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> sourceIterator = source.iterator();
        return new Iterator<T>() {
            private T nextElement = null;
            private void advance() {
                while (nextElement == null && sourceIterator.hasNext()) {
                    T candidate = sourceIterator.next();
                    if (combinedPredicate.test(candidate)) { nextElement = candidate; }
                }
            }
            @Override
            public boolean hasNext() {
                if (nextElement == null) { advance(); }
                return nextElement != null;
            }
            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T result = nextElement;
                nextElement = null;
                return result;
            }
        };
    }

    @Override
    public boolean isSizeKnown() { return false; }
    @Override
    public boolean isMaterialized() { return false; }
    @Override
    public boolean isEmpty() { return !iterator().hasNext(); }
    @Override
    public int size() {
        int count = 0;
        Iterator<T> it = iterator();
        while (it.hasNext()) { it.next(); count++; }
        return count;
    }
}
