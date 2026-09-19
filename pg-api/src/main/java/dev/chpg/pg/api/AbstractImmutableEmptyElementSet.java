package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * An empty, immutable implementation of {@link ElementSet}.
 * <p>
 * <b>What it represents:</b> A singleton representing a mathematical empty set of elements.
 * <p>
 * <b>Why it exists:</b> To prevent unnecessary memory allocations when returning empty results from graph queries.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return {@code empty()} singletons.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Returning an empty set when a query yields no elements.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe as it is an empty, immutable singleton.
 * <p>
 * <b>Performance characteristics:</b> Zero allocation overhead, O(1) for all operations.
 *
 * @param <T> the type of elements in this set
 */
public abstract class AbstractImmutableEmptyElementSet<T extends GraphElement> extends AbstractSet<T> implements ElementSet<T> {

    @SuppressWarnings("unchecked")
    @Override
    public ElementSet<T> materialize() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ElementSet<T> toImmutable() {
        return this;
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Optional<T> one() {
        return Optional.empty();
    }

    @Override
    public Set<Integer> ids() {
        return Collections.emptySet();
    }

    @Override
    public int[] toIdArray() {
        return new int[0];
    }
}
