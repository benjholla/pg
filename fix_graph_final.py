with open("pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java", "r") as f:
    text = f.read()

# 1. Update the map definitions
text = text.replace("private Map<Integer, EphemeralNode> nodes;", "private Map<Integer, Node> nodes;")
text = text.replace("private Map<Integer, EphemeralEdge> edges;", "private Map<Integer, Edge> edges;")
text = text.replace("private Map<Integer, EphemeralEdgeSet> inEdges;", "private Map<Integer, EphemeralEdgeSet> inEdges;")
text = text.replace("private Map<Integer, EphemeralEdgeSet> outEdges;", "private Map<Integer, EphemeralEdgeSet> outEdges;")


# 2. Add validateAndWrap and getTombstoned getters
# Let's add them right after createGraph()
replacement = """    @Override
    public EphemeralGraph createGraph() {
        return new EphemeralGraph(this.universe, this.idGenerator);
    }

    java.util.BitSet getTombstonedNodeIds() {
        return this.tombstonedNodeIds;
    }

    java.util.BitSet getTombstonedEdgeIds() {
        return this.tombstonedEdgeIds;
    }

    Node validateAndWrap(Node node) {
        java.util.Objects.requireNonNull(node, "Node cannot be null");
        if (node instanceof ShadowUniverseNode) {
            ShadowUniverseNode shadow = (ShadowUniverseNode) node;
            if (shadow.transaction() != this) {
                throw new IllegalArgumentException("Shadow node belongs to a foreign transaction.");
            }
            return shadow;
        }
        if (node instanceof EphemeralNode) {
            EphemeralNode ephemeral = (EphemeralNode) node;
            if (ephemeral.universe() != this.universe) {
                throw new IllegalArgumentException("Ephemeral node is bound to a foreign Universe.");
            }
            return ephemeral;
        }
        if (node instanceof dev.chpg.pg.multiverse.universe.UniverseNode) {
            dev.chpg.pg.multiverse.universe.UniverseNode universeNode = (dev.chpg.pg.multiverse.universe.UniverseNode) node;
            if (universeNode.universe() != this.universe) {
                throw new IllegalArgumentException("Universe node belongs to a foreign Universe.");
            }
            return new ShadowUniverseNode(this, universeNode);
        }
        throw new IllegalArgumentException("Unsupported Node type: " + node.getClass().getName());
    }

    Edge validateAndWrap(Edge edge) {
        java.util.Objects.requireNonNull(edge, "Edge cannot be null");
        if (edge instanceof ShadowEdge) {
            ShadowEdge shadow = (ShadowEdge) edge;
            if (shadow.transaction() != this) {
                throw new IllegalArgumentException("Shadow edge belongs to a foreign transaction.");
            }
            return shadow;
        }
        if (edge instanceof EphemeralEdge) {
            EphemeralEdge ephemeral = (EphemeralEdge) edge;
            if (ephemeral.universe() != this.universe) {
                throw new IllegalArgumentException("Ephemeral edge is bound to a foreign Universe.");
            }
            return ephemeral;
        }
        if (edge instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
            dev.chpg.pg.multiverse.universe.UniverseEdge universeEdge = (dev.chpg.pg.multiverse.universe.UniverseEdge) edge;
            if (universeEdge.universe() != this.universe) {
                throw new IllegalArgumentException("Universe edge belongs to a foreign Universe.");
            }
            return new ShadowEdge(this, universeEdge);
        }
        throw new IllegalArgumentException("Unsupported Edge type: " + edge.getClass().getName());
    }
"""

text = text.replace("""    @Override
    public EphemeralGraph createGraph() {
        return new EphemeralGraph(this.universe, this.idGenerator);
    }""", replacement)


# 3. Add Tombstone BitSet fields if they aren't there
if "java.util.BitSet tombstonedNodeIds" not in text:
    text = text.replace("private final EphemeralIdGenerator idGenerator;", """private final EphemeralIdGenerator idGenerator;
    private final java.util.BitSet tombstonedNodeIds = new java.util.BitSet();
    private final java.util.BitSet tombstonedEdgeIds = new java.util.BitSet();""")


# 4. Fix EphemeralNode iteration loops
text = text.replace("for (EphemeralNode n : this.nodes.values()) {", "for (Node n : this.nodes.values()) {")
text = text.replace("for (EphemeralEdge e : this.edges.values()) {", "for (Edge e : this.edges.values()) {")


with open("pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java", "w") as f:
    f.write(text)
