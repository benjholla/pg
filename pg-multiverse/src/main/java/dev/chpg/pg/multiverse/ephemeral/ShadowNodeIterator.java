package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import java.util.Iterator;

/**
 * A transactional iterator wrapper that dynamically shields universe nodes as they are traversed.
 * <p>
 * <b>What it represents:</b> An iterator that lazily wraps outgoing core engine elements into {@link ShadowUniverseNode} proxies.
 * <p>
 * <b>Why it exists:</b> To prevent unmodified baseline {@code UniverseNode} instances from "leaking" out of {@code EphemeralGraph} topology queries, ensuring that subsequent mutations to those elements are safely caught by the delta log.
 * <p>
 * <b>When to use it:</b> Used internally by {@link ShadowNodeSet} during traversal.
 * <p>
 * <b>Common usage patterns:</b> Functions exactly as a standard Java Iterator.
 * <p>
 * <b>Important invariants:</b> Lineage validation is enforced on every yield. If an element belongs to a foreign universe or transaction, an {@code IllegalArgumentException} is thrown. Direct removals via {@code remove()} are aggressively unsupported to force routing through the main graph API.
 * <p>
 * <b>Thread safety:</b> Inherits the thread-safety of the backing iterator. Generally not thread-safe.
 * <p>
 * <b>Performance characteristics:</b> Allocates a lightweight proxy object per element yielded, circumventing the zero-allocation ideal of the core engine, which is the required cost for isolated transactions.
 */
public class ShadowNodeIterator implements Iterator<Node> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Node> backingIterator;

    /**
     * Constructs a shielding iterator over a core engine iterator.
     *
     * @param context the transaction context managing the boundary
     * @param backingIterator the raw node iterator from the baseline universe
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
