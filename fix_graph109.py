with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Let's fix EphemeralGraph.edges(Node source, Node target)
import re

edges_old = """    @Override
    public EdgeSet edges(Node source, Node target) {
        if (!(source instanceof EphemeralNode eSource) || !nodes.containsKey(eSource.id())) { return EMPTY_EDGES; }
        if (!(target instanceof EphemeralNode eTarget) || !nodes.containsKey(eTarget.id())) { return EMPTY_EDGES; }

        Optional<EdgeSet> out = getOutEdgesFromNode(eSource);
        if (out.isEmpty()) { return EMPTY_EDGES; }

        EdgeSet result = new EphemeralEdgeSet();
        for (Edge e : out.get()) {
            if (e.to().equals(eTarget)) {
                result.add(e);
            }
        }
        return result.isEmpty() ? EMPTY_EDGES : (result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result));
    }"""

edges_new = """    @Override
    public EdgeSet edges(Node source, Node target) {
        if (!nodes.containsKey(source.id())) { return EMPTY_EDGES; }
        if (!nodes.containsKey(target.id())) { return EMPTY_EDGES; }

        Optional<EdgeSet> out = getOutEdgesFromNode(source);
        if (out.isEmpty()) { return EMPTY_EDGES; }

        EdgeSet result = new EphemeralEdgeSet();
        for (Edge e : out.get()) {
            if (e.to().equals(target) || e.to().id() == target.id()) {
                result.add(e);
            }
        }
        return result.isEmpty() ? EMPTY_EDGES : (result.size() == 1 ? new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(result.iterator().next())) : new dev.chpg.pg.api.GenericImmutableEdgeSet(result));
    }"""

content = content.replace(edges_old, edges_new)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
