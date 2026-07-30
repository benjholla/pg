import re

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Update nodes()
nodesOld = """    @Override
    public NodeSet nodes() {
        return new EphemeralUnmodifiableLiveNodeSet(nodes, edges, inEdges, outEdges);
    }"""
nodesNew = """    @Override
    public NodeSet nodes() {
        NodeSet universeNodes = dev.chpg.pg.api.NodeSet.empty();
        if (universe instanceof dev.chpg.pg.multiverse.universe.UniverseGraph) {
             universeNodes = ((dev.chpg.pg.multiverse.universe.UniverseGraph) universe).nodes();
        }
        java.util.Set<Node> localAdds = new java.util.HashSet<>();
        for (Node n : nodes.values()) {
             if (n instanceof EphemeralNode) {
                  localAdds.add(n);
             }
        }
        return new ShadowNodeSet(this, universeNodes, localAdds);
    }"""
content = content.replace(nodesOld, nodesNew)


# Update edges()
edgesOld = """    @Override
    public EdgeSet edges() {
        return new EphemeralUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
    }"""
edgesNew = """    @Override
    public EdgeSet edges() {
        EdgeSet universeEdges = dev.chpg.pg.api.EdgeSet.empty();
        if (universe instanceof dev.chpg.pg.multiverse.universe.UniverseGraph) {
             universeEdges = ((dev.chpg.pg.multiverse.universe.UniverseGraph) universe).edges();
        }
        java.util.Set<Edge> localAdds = new java.util.HashSet<>();
        for (Edge e : edges.values()) {
             if (e instanceof EphemeralEdge) {
                  localAdds.add(e);
             }
        }
        return new ShadowEdgeSet(this, universeEdges, localAdds);
    }"""
content = content.replace(edgesOld, edgesNew)


with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
