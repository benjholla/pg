package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.AbstractSet;

/**
 * A transactional view for tags on shadowed universe elements.
 */
public class ShadowTagSet extends AbstractSet<String> implements TagSet {

    private final EphemeralGraph transaction;
    private final TagSet backingTags;
    private final int id;
    private final boolean isNode;

    /**
     * Constructs a shadow tag set for a universe node.
     * @param transaction the ephemeral transaction
     * @param backingNode the backing universe node
     */
    public ShadowTagSet(EphemeralGraph transaction, UniverseNode backingNode) {
        this.transaction = transaction;
        this.backingTags = backingNode.tags();
        this.id = backingNode.id();
        this.isNode = true;
    }

    /**
     * Constructs a shadow tag set for a universe edge.
     * @param transaction the ephemeral transaction
     * @param backingEdge the backing universe edge
     */
    public ShadowTagSet(EphemeralGraph transaction, UniverseEdge backingEdge) {
        this.transaction = transaction;
        this.backingTags = backingEdge.tags();
        this.id = backingEdge.id();
        this.isNode = false;
    }

    private Set<String> getPendingTags() {
        return isNode ? transaction.getPendingNodeTags(id) : transaction.getPendingEdgeTags(id);
    }
    private Set<String> getOrComputePendingTags() {
        return isNode ? transaction.getOrComputePendingNodeTags(id) : transaction.getOrComputePendingEdgeTags(id);
    }

    private Set<String> getRemovedTags() {
        return isNode ? transaction.getRemovedNodeTags(id) : transaction.getRemovedEdgeTags(id);
    }
    private Set<String> getOrComputeRemovedTags() {
        return isNode ? transaction.getOrComputeRemovedNodeTags(id) : transaction.getOrComputeRemovedEdgeTags(id);
    }

    @Override
    public boolean add(String tag) {
        // 1. Clear any pending tombstone for this tag
        getRemovedTags().remove(tag);

        // 2. If it's already in the universe, don't buffer a redundant add
        if (backingTags.contains(tag)) {
            return false;
        }

        // 3. Buffer the addition
        return getOrComputePendingTags().add(tag);
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof String tag)) {
            return false;
        }

        // 1. Try to remove it from the uncommitted adds
        boolean removedFromPending = getPendingTags().remove(tag);

        // 2. If it exists in the backing universe, we MUST drop a tombstone
        if (backingTags.contains(tag)) {
            getOrComputeRemovedTags().add(tag);
            return true;
        }

        return removedFromPending;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof String tag)) {
            return false;
        }
        // If it's tombstoned in this transaction, it's virtually gone
        if (getRemovedTags().contains(tag)) {
            return false;
        }
        // Otherwise, it exists if it's pending OR if it's in the universe
        return getPendingTags().contains(tag) || backingTags.contains(tag);
    }

    @Override
    public int size() {
        // We calculate the virtual size by combining sets
        int baseline = backingTags.size();
        int pending = getPendingTags().size();

        // Count how many tombstones actually mask existing universe tags
        long effectiveTombstones = getRemovedTags().stream()
                .filter(backingTags::contains)
                .count();

        return (int) (baseline + pending - effectiveTombstones);
    }

    @Override
    public Iterator<String> iterator() {
        // Composite iterator: Pending Adds + (Universe Tags - Tombstones)
        Set<String> composite = new HashSet<>();

        // Manually copy backing tags since toSet() is not guaranteed to exist on all TagSets
        for (String tag : backingTags) {
            composite.add(tag);
        }

        composite.removeAll(getRemovedTags());
        composite.addAll(getPendingTags());

        return composite.iterator();
    }
}
