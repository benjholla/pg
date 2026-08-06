package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import java.util.Iterator;

/**
 * An iterator that wraps universe edges with transaction-aware shadow views.
 */
public class ShadowEdgeIterator implements Iterator<Edge> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Edge> backingIterator;

    /**
     * Constructs a new ShadowEdgeIterator.
     *
     * @param context the ephemeral context
     * @param backingIterator the base iterator to wrap elements from
     */
    public ShadowEdgeIterator(EphemeralGraph context, Iterator<Edge> backingIterator) {
        this.transactionContext = context;
        this.backingIterator = backingIterator;
    }

    @Override
    public boolean hasNext() {
        return backingIterator.hasNext();
    }

    @Override
    public Edge next() {
        Edge nextEdge = backingIterator.next();
        // The transaction context acts as the polymorphic firewall
        return transactionContext.validateAndWrap(nextEdge);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Removals must be routed explicitly through EphemeralGraph APIs.");
    }
}
