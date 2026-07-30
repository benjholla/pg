package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.UniverseView;
import java.util.Iterator;

public class ShadowEdgeIterator implements Iterator<Edge> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Edge> backingIterator;

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
