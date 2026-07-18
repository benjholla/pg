package dev.chpg.pg.global;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import dev.chpg.pg.api.TagSet;

/**
 * Tags denote set membership
 */
public final class GlobalTagSet implements TagSet {

    private final Set<String> delegate;

    public GlobalTagSet() {
        this.delegate = new HashSet<>();
    }

    public GlobalTagSet(Collection<? extends String> c) {
        this.delegate = new HashSet<>();
        this.addAll(c);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(String e) {
        return delegate.add(Objects.requireNonNull(e, "Tag cannot be null"));
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        Objects.requireNonNull(c, "Tag collection cannot be null");
        boolean modified = false;
        for (String e : c) {
            if (delegate.add(Objects.requireNonNull(e, "Tag cannot be null"))) { modified = true; }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
