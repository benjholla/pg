package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import java.util.Iterator;

/**
 * A transactional iterator that lazily shields baseline nodes with shadow proxies.
 * <p>
 * <b>What it represents:</b> An iterator over a {@code NodeSet} within a transaction boundary.
 * <p>
 * <b>Why it exists:</b> Ensures that when clients iterate over core universe topologies, the returned elements are properly wrapped to intercept subsequent property mutations.
 * <p>
 * <b>When to use it:</b> Created internally when traversing a {@code ShadowNodeSet}.
 * <p>
 * <b>Common usage patterns:</b> Standard {@code Iterator} semantics.
 * <p>
 * <b>Important invariants:</b> Validates that the underlying elements belong to the same universe engine as the transaction sandbox. Rejects cross-sandbox contamination.
 * <p>
 * <b>Thread safety:</b> Not thread-safe.
 * <p>
 * <b>Performance characteristics:</b> Incurs minimal object allocation overhead by lazily instantiating {@code ShadowUniverseNode} wrappers only as elements are yielded.
 */
public class ShadowNodeIterator implements Iterator<Node> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Node> backingIterator;

    /**
     * Constructs a new {@code ShadowNodeIterator}.
     *
     * @param context the transactional sandbox context
     * @param backingIterator the baseline iterator yielding unproxied core nodes
     */
    public ShadowNodeIterator(EphemeralGraph context, Iterator<Node> backingIterator) {
        this.transactionContext = context;
        this.backingIterator = backingIterator;
    }

    @Override
    public boolean hasNext() {
        return backingIterator.hasNext();
    }

    @Override
    public Node next() {
        Node nextNode = backingIterator.next();

        // Shield it with the transaction context before yielding it to the user.
        return transactionContext.validateAndWrap(nextNode);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Removals must be routed explicitly through EphemeralGraph APIs.");
    }
}
