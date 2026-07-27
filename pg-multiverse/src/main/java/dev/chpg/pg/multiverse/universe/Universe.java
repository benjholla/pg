package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.MultiverseIdGenerator;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Central Registry and stateful engine for a pg-multiverse instance.
 *
 * Phase 1 Shell: Establishes identity, concurrency boundaries, and
 * prepares the architectural footprint for Ephemeral promotion.
 */
public final class Universe {

    private final int universeId;
    private final UniverseIdGenerator idGenerator;

    // Initial capacity for all columnar arrays
    private static final int INITIAL_CAPACITY = 1024;

    // =========================================================================
    // TOPOLOGY ARRAYS (STRUCTURAL COLUMNAR STORAGE)
    // =========================================================================
    // Array index = Edge ID | Array value = Node ID
    private int[] edgeSources = new int[INITIAL_CAPACITY];
    private int[] edgeTargets = new int[INITIAL_CAPACITY];

    // =========================================================================
    // ADJACENCY MATRICES (STRUCTURAL COLUMNAR STORAGE)
    // =========================================================================
    // Node ID -> Array of Edge IDs
    private int[][] nodeOutEdges = new int[INITIAL_CAPACITY][];
    private int[][] nodeInEdges = new int[INITIAL_CAPACITY][];

    // =========================================================================
    // PROPERTY COLUMNAR STORAGE
    // =========================================================================
    private final Map<String, BitSet> columnarNodeTags = new ConcurrentHashMap<>();
    private final Map<String, BitSet> columnarEdgeTags = new ConcurrentHashMap<>();

    private final Map<String, AttributeValue[]> columnarNodeAttributes = new ConcurrentHashMap<>();
    private final Map<String, AttributeValue[]> columnarEdgeAttributes = new ConcurrentHashMap<>();

    /**
     * Instantiates a completely isolated Universe with its own ID space and modCount.
     */
    public Universe() {
        this.universeId = MultiverseIdGenerator.INSTANCE.createUniverseId();
        this.idGenerator = new UniverseIdGenerator();
    }

    /**
     * Optional constructor if you need to inject a specifically configured generator.
     *
     * @param idGenerator the ID generator to inject
     */
    public Universe(UniverseIdGenerator idGenerator) {
        this.universeId = MultiverseIdGenerator.INSTANCE.createUniverseId();
        this.idGenerator = Objects.requireNonNull(idGenerator, "IdGenerator cannot be null");
    }

    // =========================================================================
    // 1. IDENTITY & CONCURRENCY DELEGATION
    // =========================================================================

    /**
     * Exposes the isolated ID generator for this specific Universe instance.
     *
     * @return the isolated ID generator
     */
    public UniverseIdGenerator idGenerator() {
        return this.idGenerator;
    }

    /**
     * Returns the globally unique ID of this Universe.
     *
     * @return the unique integer universe ID
     */
    public int universeId() {
        return this.universeId;
    }

    /**
     * Returns the current modification count of this Universe.
     * Iterators and Flyweight Sets must snapshot this value upon creation and
     * validate against it during iteration to provide fail-fast concurrency.
     *
     * @return the current modification count
     */
    public long modCount() {
        return this.idGenerator.getModCount();
    }

    /**
     * Increments the Universe modification count.
     * Must be called whenever topology or global BitSet properties are altered.
     *
     * @return the incremented modification count
     */
    public long incrementModCount() {
        return this.idGenerator.incrementAndGetModCount();
    }

    // =========================================================================
    // STRUCTURAL TOPOLOGY ENGINE
    // =========================================================================

    private void ensureTopologyCapacity(int nodeId, int edgeId) {
        // Resize edge definition arrays
        if (edgeId >= edgeSources.length) {
            int newCap = Math.max(edgeSources.length * 2, edgeId + 1);
            edgeSources = Arrays.copyOf(edgeSources, newCap);
            edgeTargets = Arrays.copyOf(edgeTargets, newCap);
        }
        // Resize adjacency matrix outer arrays
        if (nodeId >= nodeOutEdges.length) {
            int newCap = Math.max(nodeOutEdges.length * 2, nodeId + 1);
            nodeOutEdges = Arrays.copyOf(nodeOutEdges, newCap);
            nodeInEdges = Arrays.copyOf(nodeInEdges, newCap);
        }
    }

    private int[] appendEdgeId(int[] currentArray, int edgeId) {
        if (currentArray == null) {
            return new int[]{edgeId};
        }
        int len = currentArray.length;
        int[] newArray = Arrays.copyOf(currentArray, len + 1);
        newArray[len] = edgeId;
        return newArray;
    }

    /**
     * Wires an edge into the structural topology.
     * Called exclusively by the engine when a new edge is allocated.
     *
     * @param edgeId the primitive ID of the edge
     * @param sourceId the source node ID
     * @param targetId the target node ID
     */
    public void wireEdge(int edgeId, int sourceId, int targetId) {
        ensureTopologyCapacity(Math.max(sourceId, targetId), edgeId);

        // 1. Define the edge endpoints
        this.edgeSources[edgeId] = sourceId;
        this.edgeTargets[edgeId] = targetId;

        // 2. Wire the Adjacency Matrices
        this.nodeOutEdges[sourceId] = appendEdgeId(this.nodeOutEdges[sourceId], edgeId);
        this.nodeInEdges[targetId] = appendEdgeId(this.nodeInEdges[targetId], edgeId);

        incrementModCount();
    }

    /**
     * Returns an exact-sized array of outbound edge IDs for the given node, or null.
     *
     * @param nodeId the node ID
     * @return an exact-sized array of outbound edge IDs
     */
    public int[] outboundEdges(int nodeId) {
        if (nodeId >= this.nodeOutEdges.length) { return null; }
        return this.nodeOutEdges[nodeId];
    }

    /**
     * Returns an exact-sized array of inbound edge IDs for the given node, or null.
     *
     * @param nodeId the node ID
     * @return an exact-sized array of inbound edge IDs
     */
    public int[] inboundEdges(int nodeId) {
        if (nodeId >= this.nodeInEdges.length) { return null; }
        return this.nodeInEdges[nodeId];
    }

    // =========================================================================
    // STRUCTURAL TOPOLOGY METHODS
    // =========================================================================

    /**
     * Resolves the source node ID for a given edge.
     *
     * @param edgeId the primitive ID of the edge
     * @return the source node ID
     */
    public int edgeSource(int edgeId) {
        if (edgeId >= this.edgeSources.length) {
            throw new IllegalArgumentException("Edge ID exceeds allocated structural capacity.");
        }
        return this.edgeSources[edgeId];
    }

    /**
     * Resolves the target node ID for a given edge.
     *
     * @param edgeId the primitive ID of the edge
     * @return the target node ID
     */
    public int edgeTarget(int edgeId) {
        if (edgeId >= this.edgeTargets.length) {
            throw new IllegalArgumentException("Edge ID exceeds allocated structural capacity.");
        }
        return this.edgeTargets[edgeId];
    }

    // =========================================================================
    // 2. PHASE 4/5 ARCHITECTURAL STUBS
    // =========================================================================

    /**
     * Promotes a write-optimized EphemeralGraph into a read-optimized UniverseGraph.
     * Deep-clones state, translates negative IDs to positive IDs, rewires topology,
     * and permanently invalidates the ephemeral sandbox.
     *
     * @param ephemeralGraph The sandbox graph to promote and invalidate.
     * @return A read-optimized, BitSet-backed view of the promoted topology.
     */
    public UniverseGraph promote(EphemeralGraph ephemeralGraph) {
        Objects.requireNonNull(ephemeralGraph, "EphemeralGraph cannot be null");

        BitSet activeNodes = new BitSet();
        BitSet activeEdges = new BitSet();

        // Local translation map: Ephemeral ID (Negative) -> Universe ID (Positive)
        // Scoped entirely to this method, instantly GC'd after promotion completes.
        Map<Integer, Integer> nodeTranslationMap = new HashMap<>();

        // 1. Bulk Ingest Nodes
        for (Node node : ephemeralGraph.nodes()) {
            int newId = this.idGenerator.createNodeId();
            nodeTranslationMap.put(node.id(), newId);
            activeNodes.set(newId);

            // Copy tags and attributes directly into the columnar arrays
            for (String tag : node.tags()) {
                this.addNodeTag(newId, tag);
            }
            for (Map.Entry<String, AttributeValue> entry : node.attributes().entrySet()) {
                this.setNodeAttribute(newId, entry.getKey(), entry.getValue());
            }
        }

        // 2. Bulk Ingest Edges
        for (Edge edge : ephemeralGraph.edges()) {
            int newEdgeId = this.idGenerator.createEdgeId();
            activeEdges.set(newEdgeId);

            // Translate the ephemeral topology endpoints to the newly generated Universe IDs
            Integer uSourceId = nodeTranslationMap.get(edge.from().id());
            Integer uTargetId = nodeTranslationMap.get(edge.to().id());

            if (uSourceId == null || uTargetId == null) {
                throw new IllegalStateException("EphemeralGraph topology invariant violated: Edge references unmapped node.");
            }

            // Wire the structural matrix
            this.wireEdge(newEdgeId, uSourceId, uTargetId);

            // Copy tags and attributes
            for (String tag : edge.tags()) {
                this.addEdgeTag(newEdgeId, tag);
            }
            for (Map.Entry<String, AttributeValue> entry : edge.attributes().entrySet()) {
                this.setEdgeAttribute(newEdgeId, entry.getKey(), entry.getValue());
            }
        }

        // 3. The Burn-the-Boats Invalidation
        // Invokes the clear() method to drop all EphemeralNode/Edge object references
        // from the EphemeralGraph's internal HashMaps. They are now completely
        // orphaned and instantly eligible for Eden-space collection.
        ephemeralGraph.clear();

        return new UniverseGraph(this, activeNodes, activeEdges);
    }

    @Override
    public String toString() {
        return "Universe[id=" + this.universeId +
               ", allocatedNodes=" + this.idGenerator.allocatedNodeCount() +
               ", allocatedEdges=" + this.idGenerator.allocatedEdgeCount() + "]";
    }

    // =========================================================================
    // NODE TAG ENGINE
    // =========================================================================

    /**
     * Returns the number of tags on a node.
     *
     * @param nodeId the node ID
     * @return the number of tags
     */
    public int nodeTagCount(int nodeId) {
        int count = 0;
        for (BitSet bits : columnarNodeTags.values()) {
            if (bits.get(nodeId)) { count++; }
        }
        return count;
    }

    /**
     * Checks if a node has a specific tag.
     *
     * @param nodeId the node ID
     * @param tag the tag
     * @return true if the node has the tag, false otherwise
     */
    public boolean hasNodeTag(int nodeId, String tag) {
        BitSet bits = columnarNodeTags.get(tag);
        return bits != null && bits.get(nodeId);
    }

    /**
     * Adds a tag to a node.
     *
     * @param nodeId the node ID
     * @param tag the tag
     * @return true if the tag was added, false if it already existed
     */
    public boolean addNodeTag(int nodeId, String tag) {
        BitSet bits = columnarNodeTags.computeIfAbsent(tag, k -> new BitSet());
        if (bits.get(nodeId)) { return false; }
        bits.set(nodeId);
        incrementModCount();
        return true;
    }

    /**
     * Removes a tag from a node.
     *
     * @param nodeId the node ID
     * @param tag the tag
     * @return true if the tag was removed, false if it did not exist
     */
    public boolean removeNodeTag(int nodeId, String tag) {
        BitSet bits = columnarNodeTags.get(tag);
        if (bits == null || !bits.get(nodeId)) { return false; }
        bits.clear(nodeId);
        incrementModCount();
        return true;
    }

    /**
     * Clears all tags from a node.
     *
     * @param nodeId the node ID
     */
    public void clearNodeTags(int nodeId) {
        boolean modified = false;
        for (BitSet bits : columnarNodeTags.values()) {
            if (bits.get(nodeId)) {
                bits.clear(nodeId);
                modified = true;
            }
        }
        if (modified) { incrementModCount(); }
    }

    /**
     * Returns an iterator over the tags of a node.
     *
     * @param nodeId the node ID
     * @return an iterator over the tags
     */
    public Iterator<String> nodeTagsIterator(int nodeId) {
        return new Iterator<String>() {
            private final Iterator<Map.Entry<String, BitSet>> internal = columnarNodeTags.entrySet().iterator();
            private String nextTag = null;

            private void advance() {
                while (nextTag == null && internal.hasNext()) {
                    Map.Entry<String, BitSet> entry = internal.next();
                    if (entry.getValue().get(nodeId)) {
                        nextTag = entry.getKey();
                    }
                }
            }

            @Override public boolean hasNext() {
                if (nextTag == null) { advance(); }
                return nextTag != null;
            }

            @Override public String next() {
                if (!hasNext()) { throw new NoSuchElementException(); }
                String res = nextTag;
                nextTag = null;
                return res;
            }
        };
    }

    // =========================================================================
    // NODE ATTRIBUTE ENGINE
    // =========================================================================

    private void ensureNodeAttributeCapacity(String key, int requiredIndex) {
        columnarNodeAttributes.compute(key, (k, arr) -> {
            if (arr == null) { return new AttributeValue[Math.max(INITIAL_CAPACITY, requiredIndex + 1)]; }
            if (requiredIndex >= arr.length) {
                int newCapacity = Math.max(arr.length * 2, requiredIndex + 1);
                return Arrays.copyOf(arr, newCapacity);
            }
            return arr;
        });
    }

    /**
     * Returns the number of attributes on a node.
     *
     * @param nodeId the node ID
     * @return the number of attributes
     */
    public int nodeAttributeCount(int nodeId) {
        int count = 0;
        for (AttributeValue[] arr : columnarNodeAttributes.values()) {
            if (nodeId < arr.length && arr[nodeId] != null) { count++; }
        }
        return count;
    }

    /**
     * Checks if a node has a specific attribute.
     *
     * @param nodeId the node ID
     * @param key the attribute key
     * @return true if the node has the attribute, false otherwise
     */
    public boolean hasNodeAttribute(int nodeId, String key) {
        AttributeValue[] arr = columnarNodeAttributes.get(key);
        return arr != null && nodeId < arr.length && arr[nodeId] != null;
    }

    /**
     * Gets an attribute from a node.
     *
     * @param nodeId the node ID
     * @param key the attribute key
     * @return the attribute value, or null if not found
     */
    public AttributeValue getNodeAttribute(int nodeId, String key) {
        AttributeValue[] arr = columnarNodeAttributes.get(key);
        if (arr == null || nodeId >= arr.length) { return null; }
        return arr[nodeId];
    }

    /**
     * Sets an attribute on a node.
     *
     * @param nodeId the node ID
     * @param key the attribute key
     * @param value the attribute value
     * @return the previous attribute value, or null if it did not exist
     */
    public AttributeValue setNodeAttribute(int nodeId, String key, AttributeValue value) {
        ensureNodeAttributeCapacity(key, nodeId);
        AttributeValue[] arr = columnarNodeAttributes.get(key);
        AttributeValue old = arr[nodeId];
        arr[nodeId] = value;
        incrementModCount();
        return old;
    }

    /**
     * Removes an attribute from a node.
     *
     * @param nodeId the node ID
     * @param key the attribute key
     * @return the removed attribute value, or null if it did not exist
     */
    public AttributeValue removeNodeAttribute(int nodeId, String key) {
        AttributeValue[] arr = columnarNodeAttributes.get(key);
        if (arr == null || nodeId >= arr.length) { return null; }
        AttributeValue old = arr[nodeId];
        if (old != null) {
            arr[nodeId] = null;
            incrementModCount();
        }
        return old;
    }

    /**
     * Clears all attributes from a node.
     *
     * @param nodeId the node ID
     */
    public void clearNodeAttributes(int nodeId) {
        boolean modified = false;
        for (AttributeValue[] arr : columnarNodeAttributes.values()) {
            if (nodeId < arr.length && arr[nodeId] != null) {
                arr[nodeId] = null;
                modified = true;
            }
        }
        if (modified) { incrementModCount(); }
    }

    /**
     * Returns an entry set of the attributes on a node.
     *
     * @param nodeId the node ID
     * @return an entry set of the attributes
     */
    public Set<Map.Entry<String, AttributeValue>> nodeAttributeEntrySet(int nodeId) {
        return new java.util.AbstractSet<Map.Entry<String, AttributeValue>>() {
            @Override public int size() { return nodeAttributeCount(nodeId); }
            @Override public Iterator<Map.Entry<String, AttributeValue>> iterator() {
                return new Iterator<Map.Entry<String, AttributeValue>>() {
                    private final Iterator<Map.Entry<String, AttributeValue[]>> internal = columnarNodeAttributes.entrySet().iterator();
                    private Map.Entry<String, AttributeValue> nextEntry = null;

                    private void advance() {
                        while (nextEntry == null && internal.hasNext()) {
                            Map.Entry<String, AttributeValue[]> entry = internal.next();
                            AttributeValue[] arr = entry.getValue();
                            if (nodeId < arr.length && arr[nodeId] != null) {
                                nextEntry = new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), arr[nodeId]);
                            }
                        }
                    }

                    @Override public boolean hasNext() {
                        if (nextEntry == null) { advance(); }
                        return nextEntry != null;
                    }

                    @Override public Map.Entry<String, AttributeValue> next() {
                        if (!hasNext()) { throw new NoSuchElementException(); }
                        Map.Entry<String, AttributeValue> res = nextEntry;
                        nextEntry = null;
                        return res;
                    }
                };
            }
        };
    }

    // =========================================================================
    // EDGE TAG ENGINE
    // =========================================================================

    /**
     * Returns the number of tags on an edge.
     *
     * @param edgeId the edge ID
     * @return the number of tags
     */
    public int edgeTagCount(int edgeId) {
        int count = 0;
        for (BitSet bits : columnarEdgeTags.values()) {
            if (bits.get(edgeId)) { count++; }
        }
        return count;
    }

    /**
     * Checks if an edge has a specific tag.
     *
     * @param edgeId the edge ID
     * @param tag the tag
     * @return true if the edge has the tag, false otherwise
     */
    public boolean hasEdgeTag(int edgeId, String tag) {
        BitSet bits = columnarEdgeTags.get(tag);
        return bits != null && bits.get(edgeId);
    }

    /**
     * Adds a tag to an edge.
     *
     * @param edgeId the edge ID
     * @param tag the tag
     * @return true if the tag was added, false if it already existed
     */
    public boolean addEdgeTag(int edgeId, String tag) {
        BitSet bits = columnarEdgeTags.computeIfAbsent(tag, k -> new BitSet());
        if (bits.get(edgeId)) { return false; }
        bits.set(edgeId);
        incrementModCount();
        return true;
    }

    /**
     * Removes a tag from an edge.
     *
     * @param edgeId the edge ID
     * @param tag the tag
     * @return true if the tag was removed, false if it did not exist
     */
    public boolean removeEdgeTag(int edgeId, String tag) {
        BitSet bits = columnarEdgeTags.get(tag);
        if (bits == null || !bits.get(edgeId)) { return false; }
        bits.clear(edgeId);
        incrementModCount();
        return true;
    }

    /**
     * Clears all tags from an edge.
     *
     * @param edgeId the edge ID
     */
    public void clearEdgeTags(int edgeId) {
        boolean modified = false;
        for (BitSet bits : columnarEdgeTags.values()) {
            if (bits.get(edgeId)) {
                bits.clear(edgeId);
                modified = true;
            }
        }
        if (modified) { incrementModCount(); }
    }

    /**
     * Returns an iterator over the tags of an edge.
     *
     * @param edgeId the edge ID
     * @return an iterator over the tags
     */
    public Iterator<String> edgeTagsIterator(int edgeId) {
        return new Iterator<String>() {
            private final Iterator<Map.Entry<String, BitSet>> internal = columnarEdgeTags.entrySet().iterator();
            private String nextTag = null;

            private void advance() {
                while (nextTag == null && internal.hasNext()) {
                    Map.Entry<String, BitSet> entry = internal.next();
                    if (entry.getValue().get(edgeId)) {
                        nextTag = entry.getKey();
                    }
                }
            }

            @Override public boolean hasNext() {
                if (nextTag == null) { advance(); }
                return nextTag != null;
            }

            @Override public String next() {
                if (!hasNext()) { throw new NoSuchElementException(); }
                String res = nextTag;
                nextTag = null;
                return res;
            }
        };
    }

    // =========================================================================
    // EDGE ATTRIBUTE ENGINE
    // =========================================================================

    private void ensureEdgeAttributeCapacity(String key, int requiredIndex) {
        columnarEdgeAttributes.compute(key, (k, arr) -> {
            if (arr == null) { return new AttributeValue[Math.max(INITIAL_CAPACITY, requiredIndex + 1)]; }
            if (requiredIndex >= arr.length) {
                int newCapacity = Math.max(arr.length * 2, requiredIndex + 1);
                return Arrays.copyOf(arr, newCapacity);
            }
            return arr;
        });
    }

    /**
     * Returns the number of attributes on an edge.
     *
     * @param edgeId the edge ID
     * @return the number of attributes
     */
    public int edgeAttributeCount(int edgeId) {
        int count = 0;
        for (AttributeValue[] arr : columnarEdgeAttributes.values()) {
            if (edgeId < arr.length && arr[edgeId] != null) { count++; }
        }
        return count;
    }

    /**
     * Checks if an edge has a specific attribute.
     *
     * @param edgeId the edge ID
     * @param key the attribute key
     * @return true if the edge has the attribute, false otherwise
     */
    public boolean hasEdgeAttribute(int edgeId, String key) {
        AttributeValue[] arr = columnarEdgeAttributes.get(key);
        return arr != null && edgeId < arr.length && arr[edgeId] != null;
    }

    /**
     * Gets an attribute from an edge.
     *
     * @param edgeId the edge ID
     * @param key the attribute key
     * @return the attribute value, or null if not found
     */
    public AttributeValue getEdgeAttribute(int edgeId, String key) {
        AttributeValue[] arr = columnarEdgeAttributes.get(key);
        if (arr == null || edgeId >= arr.length) { return null; }
        return arr[edgeId];
    }

    /**
     * Sets an attribute on an edge.
     *
     * @param edgeId the edge ID
     * @param key the attribute key
     * @param value the attribute value
     * @return the previous attribute value, or null if it did not exist
     */
    public AttributeValue setEdgeAttribute(int edgeId, String key, AttributeValue value) {
        ensureEdgeAttributeCapacity(key, edgeId);
        AttributeValue[] arr = columnarEdgeAttributes.get(key);
        AttributeValue old = arr[edgeId];
        arr[edgeId] = value;
        incrementModCount();
        return old;
    }

    /**
     * Removes an attribute from an edge.
     *
     * @param edgeId the edge ID
     * @param key the attribute key
     * @return the removed attribute value, or null if it did not exist
     */
    public AttributeValue removeEdgeAttribute(int edgeId, String key) {
        AttributeValue[] arr = columnarEdgeAttributes.get(key);
        if (arr == null || edgeId >= arr.length) { return null; }
        AttributeValue old = arr[edgeId];
        if (old != null) {
            arr[edgeId] = null;
            incrementModCount();
        }
        return old;
    }

    /**
     * Clears all attributes from an edge.
     *
     * @param edgeId the edge ID
     */
    public void clearEdgeAttributes(int edgeId) {
        boolean modified = false;
        for (AttributeValue[] arr : columnarEdgeAttributes.values()) {
            if (edgeId < arr.length && arr[edgeId] != null) {
                arr[edgeId] = null;
                modified = true;
            }
        }
        if (modified) { incrementModCount(); }
    }

    /**
     * Returns an entry set of the attributes on an edge.
     *
     * @param edgeId the edge ID
     * @return an entry set of the attributes
     */
    public Set<Map.Entry<String, AttributeValue>> edgeAttributeEntrySet(int edgeId) {
        return new java.util.AbstractSet<Map.Entry<String, AttributeValue>>() {
            @Override public int size() { return edgeAttributeCount(edgeId); }
            @Override public Iterator<Map.Entry<String, AttributeValue>> iterator() {
                return new Iterator<Map.Entry<String, AttributeValue>>() {
                    private final Iterator<Map.Entry<String, AttributeValue[]>> internal = columnarEdgeAttributes.entrySet().iterator();
                    private Map.Entry<String, AttributeValue> nextEntry = null;

                    private void advance() {
                        while (nextEntry == null && internal.hasNext()) {
                            Map.Entry<String, AttributeValue[]> entry = internal.next();
                            AttributeValue[] arr = entry.getValue();
                            if (edgeId < arr.length && arr[edgeId] != null) {
                                nextEntry = new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), arr[edgeId]);
                            }
                        }
                    }

                    @Override public boolean hasNext() {
                        if (nextEntry == null) { advance(); }
                        return nextEntry != null;
                    }

                    @Override public Map.Entry<String, AttributeValue> next() {
                        if (!hasNext()) { throw new NoSuchElementException(); }
                        Map.Entry<String, AttributeValue> res = nextEntry;
                        nextEntry = null;
                        return res;
                    }
                };
            }
        };
    }
}
