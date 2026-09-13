package dev.chpg.pg.api;

import java.util.Collection;

/**
 * A generic, immutable implementation of {@link NodeSet}.
 * <p>
 * <b>What it represents:</b> An unmodifiable, materialized collection of nodes.
 * <p>
 * <b>Why it exists:</b> To provide a guaranteed safe snapshot of nodes that cannot be altered, ensuring query results remain stable even if the underlying graph mutates.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return materialized results from operations like {@link NodeSet#materialize()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Caching stable query results for repeated analysis.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe for reading because the internal state is fundamentally unmodifiable.
 * <p>
 * <b>Performance characteristics:</b> Requires O(N) memory allocation to materialize the underlying objects, but provides fast O(1) size checks and O(1) containment checks.
 */
public final class GenericImmutableNodeSet extends AbstractGenericImmutableElementSet<Node, NodeSet> implements NodeSet {

    /**
     * Constructs a new generic immutable node set from the provided elements.
     *
     * @param elements the collection of nodes
     */
    public GenericImmutableNodeSet(Collection<? extends Node> elements) {
        super(elements);
    }

    @Override
    protected NodeSet emptySet() {
        return NodeSet.empty();
    }

    @Override
    protected NodeSet createImmutable(Collection<? extends Node> elements) {
        return new GenericImmutableNodeSet(elements);
    }
}
