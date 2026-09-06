package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A deferred evaluation pipeline for graph edges.
 * <p>
 * <b>What it represents:</b> A lazily evaluated wrapper around an existing {@link EdgeSet} that applies filtering predicates.
 * <p>
 * <b>Why it exists:</b> To prevent functional query pipelines from prematurely allocating memory when chained together. Filters are compounded and only execute when a terminal operation is called.
 * <p>
 * <b>When to use it:</b> Primarily used internally by the API when operations like {@link EdgeSet#withAttribute(String)} are invoked.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Chaining multiple filters before calling {@code size()} or {@code toIdArray()}.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Relies on the thread safety of the underlying {@code EdgeSet}. If the underlying graph changes during evaluation, a {@code ConcurrentModificationException} may occur.
 * <p>
 * <b>Performance characteristics:</b> Defers computation (O(1) creation). Terminal operations require O(N) evaluation time over the original set.
 */
public class DeferredEdgeSet extends AbstractDeferredElementSet<Edge> implements EdgeSet {

    /**
     * Constructs a new deferred edge set.
     *
     * @param source           the underlying source edge set
     * @param initialPredicate the initial filtering predicate
     */
    public DeferredEdgeSet(EdgeSet source, Predicate<Edge> initialPredicate) {
        super(source, initialPredicate);
    }

    @Override
    protected EdgeSet wrap(ElementSet<Edge> source, Predicate<Edge> predicate) {
        return new DeferredEdgeSet((EdgeSet) source, predicate);
    }

    @Override
    public EdgeSet withAttribute(String attribute) {
        return (EdgeSet) super.withAttribute(attribute);
    }

    @Override
    public EdgeSet withAttribute(String attribute, AttributeValue... values) {
        return (EdgeSet) super.withAttribute(attribute, values);
    }

    @Override
    public EdgeSet withAnyTag(String... tags) {
        return (EdgeSet) super.withAnyTag(tags);
    }

    @Override
    public EdgeSet withAllTags(String... tags) {
        return (EdgeSet) super.withAllTags(tags);
    }

    @Override
    public EdgeSet materialize() {
        Set<Edge> materialized = new java.util.HashSet<>();
        for (Edge e : this) {
            materialized.add(e);
        }
        return materialized.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(java.util.Collections.unmodifiableSet(materialized));
    }

    @Override
    public EdgeSet toImmutable() {
        return materialize();
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        return (EdgeSet) super.intersect(other);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        return (EdgeSet) super.difference(other);
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        return (EdgeSet) super.union(other);
    }
}
