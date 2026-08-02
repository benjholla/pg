package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import java.util.Iterator;

/**
 * A transactional view over a Universe node iterator.
 */
public class ShadowNodeIterator implements Iterator<Node> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Node> backingIterator;

    /**
     * Constructs a new ShadowNodeIterator.
     * @param context the ephemeral graph
     * @param backingIterator the backing universe iterator
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
