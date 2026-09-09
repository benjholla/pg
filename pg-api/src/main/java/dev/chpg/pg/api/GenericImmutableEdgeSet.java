package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Set;

/**
 * A generic, immutable implementation of {@link EdgeSet}.
 * <p>
 * <b>What it represents:</b> An unmodifiable, materialized collection of edges.
 * <p>
 * <b>Why it exists:</b> To provide a guaranteed safe snapshot of edges that cannot be altered, ensuring query results remain stable even if the underlying graph mutates.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return materialized results from operations like {@link EdgeSet#materialize()}.
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
public final class GenericImmutableEdgeSet extends AbstractGenericImmutableElementSet<Edge, EdgeSet> implements EdgeSet {

    /**
     * Constructs a new generic immutable edge set from the provided elements.
     *
     * @param elements the collection of edges
     */
    public GenericImmutableEdgeSet(Collection<? extends Edge> elements) {
        super(elements);
    }

    @Override
    protected EdgeSet emptySet() {
        return EdgeSet.empty();
    }

    @Override
    protected EdgeSet createImmutable(Set<Edge> elements) {
        return new GenericImmutableEdgeSet(elements);
    }
}
