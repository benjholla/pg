package io.github.benjholla.pg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tags denote set membership
 */
public class TagSet implements Collection<String> {

    private Set<String> tags = new HashSet<>();
    
    @Override
    public int size() {
        return tags.size();
    }

    @Override
    public boolean isEmpty() {
        return tags.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return tags.contains(o);
    }

    @Override
    public Object[] toArray() {
        return tags.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return tags.toArray(a);
    }

    @Override
    public boolean add(String e) {
        return tags.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return tags.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return tags.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        return tags.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return tags.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return tags.retainAll(c);
    }

    @Override
    public void clear() {
        tags.clear();
    }
    
    @Override
    public Iterator<String> iterator() {
        return tags.iterator();
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TagSet)) {
            return false;
        }
        TagSet other = (TagSet) obj;
        return Objects.equals(tags, other.tags);
    }

    @Override
    public String toString() {
        return "TagSet [tags=" + tags.stream().collect(Collectors.joining(", ")) + "]";
    }

}
