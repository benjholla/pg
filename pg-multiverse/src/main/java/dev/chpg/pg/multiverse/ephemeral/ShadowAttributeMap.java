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
 * A proxy {@link AttributeMap} that manages the pending attribute updates and deletions
 * within an {@link EphemeralGraph} context while reading from the underlying backing engine
 * as a baseline.
 */
public class ShadowAttributeMap extends AbstractMap<String, AttributeValue> implements AttributeMap {

    private final EphemeralGraph transaction;
    private final AttributeMap backingAttributes;
    private final int id;
    private final boolean isNode;

    /**
     * Constructs a ShadowAttributeMap for a given backing node.
     *
     * @param transaction the ephemeral transaction context
     * @param backingNode the original universe node
     */
    public ShadowAttributeMap(EphemeralGraph transaction, UniverseNode backingNode) {
        this.transaction = transaction;
        this.backingAttributes = backingNode.attributes();
        this.id = backingNode.id();
        this.isNode = true;
    }

    /**
     * Constructs a ShadowAttributeMap for a given backing edge.
     *
     * @param transaction the ephemeral transaction context
     * @param backingEdge the original universe edge
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
