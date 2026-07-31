package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.UniverseView;
import java.util.Iterator;

/**
 * A transactional iterator wrapper that dynamically shields universe edges as they are traversed.
 * <p>
 * <b>What it represents:</b> An iterator that lazily wraps outgoing core engine elements into {@link ShadowEdge} proxies.
 * <p>
 * <b>Why it exists:</b> To prevent unmodified baseline {@code UniverseEdge} instances from "leaking" out of {@code EphemeralGraph} topology queries, ensuring that subsequent mutations to those elements are safely caught by the delta log.
 * <p>
 * <b>When to use it:</b> Used internally by {@link ShadowEdgeSet} during traversal.
 * <p>
 * <b>Common usage patterns:</b> Functions exactly as a standard Java Iterator.
 * <p>
 * <b>Important invariants:</b> Lineage validation is enforced on every yield. If an element belongs to a foreign universe or transaction, an {@code IllegalArgumentException} is thrown. Direct removals via {@code remove()} are aggressively unsupported to force routing through the main graph API.
 * <p>
 * <b>Thread safety:</b> Inherits the thread-safety of the backing iterator. Generally not thread-safe.
 * <p>
 * <b>Performance characteristics:</b> Allocates a lightweight proxy object per element yielded, circumventing the zero-allocation ideal of the core engine, which is the required cost for isolated transactions.
 */
public class ShadowEdgeIterator implements Iterator<Edge> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Edge> backingIterator;

    /**
     * Constructs a shielding iterator over a core engine iterator.
     *
     * @param context the transaction context managing the boundary
     * @param backingIterator the raw edge iterator from the baseline universe
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

        if (nextEdge instanceof ShadowEdge) {
            ShadowEdge se = (ShadowEdge) nextEdge;
            if (se.transaction() != transactionContext) {
                throw new IllegalArgumentException("Shadow edge belongs to a foreign transaction.");
            }
            return se;
        }

        if (nextEdge instanceof UniverseView view) {
            if (view.universe() != transactionContext.universe()) {
                throw new IllegalArgumentException("Edge belongs to a foreign Universe.");
            }
        }

        return new ShadowEdge(transactionContext, nextEdge);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Removals must be routed explicitly through EphemeralGraph APIs.");
    }
}
