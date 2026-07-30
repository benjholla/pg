with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Let's fix EphemeralGraph.adjacent(Node source, Node target)
import re

adj_old = """    @Override
    public boolean adjacent(Node source, Node target) {
        if (!(source instanceof EphemeralNode eSource) || !nodes.containsKey(eSource.id())) { return false; }
        if (!(target instanceof EphemeralNode eTarget) || !nodes.containsKey(eTarget.id())) { return false; }

        Optional<EdgeSet> out = getOutEdgesFromNode(eSource);
        if (out.isEmpty()) { return false; }

        for (Edge e : out.get()) {
            if (e.to().equals(eTarget)) {
                return true;
            }
        }
        return false;
    }"""

adj_new = """    @Override
    public boolean adjacent(Node source, Node target) {
        if (!nodes.containsKey(source.id())) { return false; }
        if (!nodes.containsKey(target.id())) { return false; }

        Optional<EdgeSet> out = getOutEdgesFromNode(source);
        if (out.isEmpty()) { return false; }

        for (Edge e : out.get()) {
            if (e.to().equals(target) || e.to().id() == target.id()) {
                return true;
            }
        }
        return false;
    }"""

content = content.replace(adj_old, adj_new)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
