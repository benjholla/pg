with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Add `private boolean isCleared = false;` to EphemeralGraph
content = content.replace("private final EphemeralIdGenerator idGenerator;", "private final EphemeralIdGenerator idGenerator;\n    private boolean isCleared = false;")

# In clear()
clearOld = """    public void clear() {
        clearEdges();
        nodes.clear();
    }"""
clearNew = """    public void clear() {
        clearEdges();
        nodes.clear();
        isCleared = true;
    }"""
content = content.replace(clearOld, clearNew)

# In nodes()
nodesOld = """    @Override
    public NodeSet nodes() {
        NodeSet baseline = new dev.chpg.pg.multiverse.universe.UniverseNodeSet(
            this.universe,
            this.universe.activeNodeIds()
        );"""
nodesNew = """    @Override
    public NodeSet nodes() {
        if (isCleared) return dev.chpg.pg.api.NodeSet.empty();
        NodeSet baseline = new dev.chpg.pg.multiverse.universe.UniverseNodeSet(
            this.universe,
            this.universe.activeNodeIds()
        );"""
content = content.replace(nodesOld, nodesNew)

# In edges()
edgesOld = """    @Override
    public EdgeSet edges() {
        EdgeSet baseline = new dev.chpg.pg.multiverse.universe.UniverseEdgeSet(
            this.universe,
            this.universe.activeEdgeIds()
        );"""
edgesNew = """    @Override
    public EdgeSet edges() {
        if (isCleared) return dev.chpg.pg.api.EdgeSet.empty();
        EdgeSet baseline = new dev.chpg.pg.multiverse.universe.UniverseEdgeSet(
            this.universe,
            this.universe.activeEdgeIds()
        );"""
content = content.replace(edgesOld, edgesNew)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
