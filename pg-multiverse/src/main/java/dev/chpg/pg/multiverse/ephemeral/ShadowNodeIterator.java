package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import java.util.Iterator;

/**
 * An iterator over nodes that dynamically wraps baseline nodes in shadow transactions.
 */
public class ShadowNodeIterator implements Iterator<Node> {

    private final EphemeralGraph transactionContext;
    private final Iterator<Node> backingIterator;

    /**
     * Constructs a ShadowNodeIterator.
     * @param context the ephemeral transaction
     * @param backingIterator the base iterator to wrap
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
