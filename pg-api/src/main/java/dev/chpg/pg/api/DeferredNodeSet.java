package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A deferred evaluation pipeline for graph nodes.
 * <p>
 * <b>What it represents:</b> A lazily evaluated wrapper around an existing {@link NodeSet} that applies filtering predicates.
 * <p>
 * <b>Why it exists:</b> To prevent functional query pipelines from prematurely allocating memory when chained together. Filters are compounded and only execute when a terminal operation is called.
 * <p>
 * <b>When to use it:</b> Primarily used internally by the API when operations like {@link NodeSet#withAttribute(String)} are invoked.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Chaining multiple filters before calling {@code size()} or {@code toIdArray()}.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Relies on the thread safety of the underlying {@code NodeSet}. If the underlying graph changes during evaluation, a {@code ConcurrentModificationException} may occur.
 * <p>
 * <b>Performance characteristics:</b> Defers computation (O(1) creation). Terminal operations require O(N) evaluation time over the original set.
 */
public class DeferredNodeSet extends AbstractDeferredElementSet<Node> implements NodeSet {

    /**
     * Constructs a new deferred node set.
     *
     * @param source           the underlying source node set
     * @param initialPredicate the initial filtering predicate
     */
    public DeferredNodeSet(NodeSet source, Predicate<Node> initialPredicate) {
        super(source, initialPredicate);
    }

    @Override
    protected NodeSet wrap(ElementSet<Node> source, Predicate<Node> predicate) {
        return new DeferredNodeSet((NodeSet) source, predicate);
    }

    @Override
    public NodeSet withAttribute(String attribute) {
        return (NodeSet) super.withAttribute(attribute);
    }

    @Override
    public NodeSet withAttribute(String attribute, AttributeValue... values) {
        return (NodeSet) super.withAttribute(attribute, values);
    }

    @Override
    public NodeSet withAnyTag(String... tags) {
        return (NodeSet) super.withAnyTag(tags);
    }

    @Override
    public NodeSet withAllTags(String... tags) {
        return (NodeSet) super.withAllTags(tags);
    }

    @Override
    public NodeSet materialize() {
        Set<Node> materialized = new java.util.HashSet<>();
        for (Node n : this) {
            materialized.add(n);
        }
        return materialized.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(java.util.Collections.unmodifiableSet(materialized));
    }

    @Override
    public NodeSet toImmutable() {
        return materialize();
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        return (NodeSet) super.intersect(other);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        return (NodeSet) super.difference(other);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        return (NodeSet) super.union(other);
    }
}
