package dev.chpg.pg.api;

import java.util.AbstractSet;
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

    @Override
    public java.util.Optional<T> one() {
        Iterator<T> it = iterator();
        if (it.hasNext()) { return java.util.Optional.of(it.next()); }
        return java.util.Optional.empty();
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
    public boolean contains(Object o) {
        if (!(o instanceof GraphElement)) { return false; }
        @SuppressWarnings("unchecked")
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
