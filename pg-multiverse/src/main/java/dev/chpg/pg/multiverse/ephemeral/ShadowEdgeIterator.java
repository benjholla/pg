package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.UniverseView;
import java.util.Iterator;

/**
 * A transactional iterator that lazily shields baseline edges with shadow proxies.
 * <p>
 * <b>What it represents:</b> An iterator over an {@code EdgeSet} within a transaction boundary.
 * <p>
 * <b>Why it exists:</b> Ensures that when clients iterate over core universe topologies, the returned elements are properly wrapped to intercept subsequent property mutations.
 * <p>
 * <b>When to use it:</b> Created internally when traversing a {@code ShadowEdgeSet}.
 * <p>
 * <b>Common usage patterns:</b> Standard {@code Iterator} semantics.
 * <p>
 * <b>Important invariants:</b> Validates that the underlying elements belong to the same universe engine as the transaction sandbox. Rejects cross-sandbox contamination.
 * <p>
 * <b>Thread safety:</b> Not thread-safe.
 * <p>
 * <b>Performance characteristics:</b> Incurs minimal object allocation overhead by lazily instantiating {@code ShadowEdge} wrappers only as elements are yielded.
 */
public class ShadowEdgeIterator implements Iterator<Edge> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Edge> backingIterator;

    /**
     * Constructs a new {@code ShadowEdgeIterator}.
     *
     * @param context the transactional sandbox context
     * @param backingIterator the baseline iterator yielding unproxied core edges
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
