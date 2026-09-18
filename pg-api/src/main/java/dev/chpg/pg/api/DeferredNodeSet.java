package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
public class DeferredNodeSet extends AbstractDeferredElementSet<Node, NodeSet> implements NodeSet {
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
    public NodeSet withAttribute(String attribute) {
        return new DeferredNodeSet(
            (NodeSet) this.source,
            this.combinedPredicate.and(node -> node.attributes().containsKey(attribute))
        );
    }

    @Override
    public NodeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredNodeSet(
            (NodeSet) this.source,
            this.combinedPredicate.and(node -> {
                AttributeValue val = node.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) { return false; }
                return java.util.Arrays.asList(values).contains(val);
            })
        );
    }

    @Override
    public NodeSet withAnyTag(String... tags) {
        return new DeferredNodeSet(
            (NodeSet) this.source,
            this.combinedPredicate.and(node -> {
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (node.tags().contains(tag)) { return true; }
                }
                return false;
            })
        );
    }

    @Override
    public NodeSet withAllTags(String... tags) {
        return new DeferredNodeSet(
            (NodeSet) this.source,
            this.combinedPredicate.and(node -> {
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (!node.tags().contains(tag)) { return false; }
                }
                return true;
            })
        );
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
    public NodeSet intersect(Collection<? extends Node> other) {
        return new DeferredNodeSet(
            (NodeSet) this.source,
            this.combinedPredicate.and(other::contains)
        );
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        return new DeferredNodeSet(
            (NodeSet) this.source,
            this.combinedPredicate.and(n -> !other.contains(n))
        );
    }









}
