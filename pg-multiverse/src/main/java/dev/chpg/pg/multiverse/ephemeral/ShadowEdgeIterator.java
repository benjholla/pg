package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import java.util.Iterator;

/**
 * An iterator over edges that dynamically wraps baseline edges in shadow transactions.
 */
public class ShadowEdgeIterator implements Iterator<Edge> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Edge> backingIterator;

    /**
     * Constructs a ShadowEdgeIterator.
     * @param context the ephemeral transaction
     * @param backingIterator the base iterator to wrap
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
