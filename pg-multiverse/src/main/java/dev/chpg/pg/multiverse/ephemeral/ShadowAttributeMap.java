package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A transactional view over an element's attributes within an {@code EphemeralGraph}.
 * <p>
 * <b>What it represents:</b> A composite {@code Map} blending uncommitted local modifications (the delta log) with the baseline state of a permanent {@code Universe} element.
 * <p>
 * <b>Why it exists:</b> It enables the {@code EphemeralGraph} to act as a seamless write-buffer. Reads reflect a coherent merged state (baseline + pending - tombstones), while writes are routed exclusively to the local transaction, leaving the core engine pristine until promotion.
 * <p>
 * <b>When to use it:</b> Used internally by shadow wrappers (like {@code ShadowUniverseNode}) when a client accesses the {@code attributes()} property of an element within a transaction.
 * <p>
 * <b>Common usage patterns:</b> Operates identically to a standard {@code Map<String, AttributeValue>} for both querying and mutating properties.
 * <p>
 * <b>Important invariants:</b> The backing {@code Universe} element is never mutated directly. Tombstones override baseline values for deletions.
 * <p>
 * <b>Thread safety:</b> Not thread-safe. Concurrent modifications must be externally synchronized.
 * <p>
 * <b>Performance characteristics:</b> Reads may require checking up to three maps (tombstones, pending, baseline). Iteration requires allocating a composite HashMap to ensure correctness.
 */
public class ShadowAttributeMap extends AbstractMap<String, AttributeValue> implements AttributeMap {

    private final EphemeralGraph transaction;
    private final AttributeMap backingAttributes;
    private final int id;
    private final boolean isNode;

    /**
     * Constructs a {@code ShadowAttributeMap} for a node.
     *
     * @param transaction the transactional sandbox context
     * @param backingNode the baseline universe node being shadowed
     */
    public ShadowAttributeMap(EphemeralGraph transaction, UniverseNode backingNode) {
        this.transaction = transaction;
        this.backingAttributes = backingNode.attributes();
        this.id = backingNode.id();
        this.isNode = true;
    }

    /**
     * Constructs a {@code ShadowAttributeMap} for an edge.
     *
     * @param transaction the transactional sandbox context
     * @param backingEdge the baseline universe edge being shadowed
     */
    public ShadowAttributeMap(EphemeralGraph transaction, UniverseEdge backingEdge) {
        this.transaction = transaction;
        this.backingAttributes = backingEdge.attributes();
        this.id = backingEdge.id();
        this.isNode = false;
    }

    private Map<String, AttributeValue> getPendingAttributes() {
        return isNode ? transaction.getPendingNodeAttributes(id) : transaction.getPendingEdgeAttributes(id);
    }
    private Map<String, AttributeValue> getOrComputePendingAttributes() {
        return isNode ? transaction.getOrComputePendingNodeAttributes(id) : transaction.getOrComputePendingEdgeAttributes(id);
    }

    private Set<String> getRemovedAttributes() {
        return isNode ? transaction.getRemovedNodeAttributes(id) : transaction.getRemovedEdgeAttributes(id);
    }
    private Set<String> getOrComputeRemovedAttributes() {
        return isNode ? transaction.getOrComputeRemovedNodeAttributes(id) : transaction.getOrComputeRemovedEdgeAttributes(id);
    }

    @Override
    public AttributeValue put(String key, AttributeValue value) {
        // Clear any tombstone for this key
        getRemovedAttributes().remove(key);

        // Buffer the overwrite/addition
        AttributeValue previousPending = getOrComputePendingAttributes().put(key, value);

        // Return standard map semantics (previous value if existed)
        if (previousPending != null) {
            return previousPending;
        }
        return backingAttributes.get(key);
    }

    @Override
    public AttributeValue put(String key, String value) { return put(key, AttributeValue.value(value)); }
    @Override
    public AttributeValue put(String key, int value) { return put(key, AttributeValue.value(value)); }
    @Override
    public AttributeValue put(String key, long value) { return put(key, AttributeValue.value(value)); }
    @Override
    public AttributeValue put(String key, double value) { return put(key, AttributeValue.value(value)); }
    @Override
    public AttributeValue put(String key, boolean value) { return put(key, AttributeValue.value(value)); }
    @Override
    public AttributeValue put(String key, byte[] value) { return put(key, AttributeValue.value(value)); }

    @Override
    public AttributeValue get(Object key) {
        if (!(key instanceof String strKey)) {
            return null;
        }
        // 1. Is it explicitly deleted in this transaction?
        if (getRemovedAttributes().contains(strKey)) {
            return null;
        }
        // 2. Is there a pending uncommitted overwrite?
        AttributeValue pending = getPendingAttributes().get(strKey);
        if (pending != null) {
            return pending;
        }
        // 3. Fall back to the core universe engine
        return backingAttributes.get(strKey);
    }

    @Override
    public AttributeValue remove(Object key) {
        if (!(key instanceof String strKey)) {
            return null;
        }
        AttributeValue pendingVal = getPendingAttributes().remove(strKey);
        AttributeValue universeVal = backingAttributes.get(strKey);

        // If it exists in the backing universe, drop a tombstone to mask it
        if (universeVal != null) {
            getOrComputeRemovedAttributes().add(strKey);
        }

        return pendingVal != null ? pendingVal : universeVal;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String strKey)) {
            return false;
        }
        if (getRemovedAttributes().contains(strKey)) {
            return false;
        }
        return getPendingAttributes().containsKey(strKey) ||
               backingAttributes.containsKey(strKey);
    }

    @Override
    public Set<Map.Entry<String, AttributeValue>> entrySet() {
        // To support standard iteration, we compute the composite state.
        // Because attributes are rarely iterated sequentially compared to topology,
        // a local HashMap allocation here is an acceptable cost.
        Map<String, AttributeValue> composite = new HashMap<>();

        // Load Universe baseline
        for (Map.Entry<String, AttributeValue> entry : backingAttributes.entrySet()) {
            composite.put(entry.getKey(), entry.getValue());
        }

        // Apply Tombstones
        for (String tombstone : getRemovedAttributes()) {
            composite.remove(tombstone);
        }

        // Apply Pending Writes
        composite.putAll(getPendingAttributes());

        return composite.entrySet();
    }
}
