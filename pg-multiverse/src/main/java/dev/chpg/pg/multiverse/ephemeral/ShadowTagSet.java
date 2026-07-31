package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.AbstractSet;

/**
 * A transactional viewport for {@link TagSet} operations over a core {@link dev.chpg.pg.multiverse.universe.Universe} element.
 * <p>
 * <b>What it represents:</b> A composite tag set that overlays uncommitted string tags and tombstones atop a persistent baseline tag array.
 * <p>
 * <b>Why it exists:</b> To allow an {@link EphemeralGraph} to track pending string tag additions or removals without contaminating the global columnar arrays until explicitly promoted.
 * <p>
 * <b>When to use it:</b> Instantiated automatically when the {@code tags()} collection of a shielded element is accessed. Should not be constructed directly.
 * <p>
 * <b>Common usage patterns:</b> Consumers use standard {@link java.util.Set} methods (e.g., {@code add}, {@code remove}, {@code contains}). The set intercepts writes and routes them to the transaction's Delta Log while masking removed tags via tombstones.
 * <p>
 * <b>Important invariants:</b> The underlying Universe tag set is strictly read-only within this context. Redundant additions (adding a tag already in the baseline) are safely ignored. Removing a baseline tag drops a tombstone.
 * <p>
 * <b>Thread safety:</b> Not thread-safe. Concurrent modifications to the same transaction require external synchronization.
 * <p>
 * <b>Performance characteristics:</b> Lookups ({@code contains}) evaluate local HashSets before checking the baseline BitSets. Sizing ({@code size()}) is O(T) relative to the number of active tombstones, as effective sizes require streaming filters.
 */
public class ShadowTagSet extends AbstractSet<String> implements TagSet {

    private final EphemeralGraph transaction;
    private final TagSet backingTags;
    private final int id;
    private final boolean isNode;

    /**
     * Constructs a ShadowTagSet overlay for a {@link UniverseNode}.
     *
     * @param transaction the isolated graph context managing the delta log
     * @param backingNode the core universe element acting as the read baseline
     */
    public ShadowTagSet(EphemeralGraph transaction, UniverseNode backingNode) {
        this.transaction = transaction;
        this.backingTags = backingNode.tags();
        this.id = backingNode.id();
        this.isNode = true;
    }

    /**
     * Constructs a ShadowTagSet overlay for a {@link UniverseEdge}.
     *
     * @param transaction the isolated graph context managing the delta log
     * @param backingEdge the core universe element acting as the read baseline
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
        return isNode ? transaction.getPendingNodeTags(id) : transaction.getPendingEdgeTags(id);
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
