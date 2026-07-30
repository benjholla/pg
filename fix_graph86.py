# Ah! `transactionContext.getTombstonedEdgeIds().get(shadow.id())` where `shadow.id()` is negative!
# Because an `EphemeralEdge` wrapped in a `ShadowEdge` has a negative ID!
# We must ensure we only check tombstones for positive IDs (Universe items).

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

import re

# Update contains to not check negative IDs in tombstonedNodeIds
contains_node = """    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Node node)) return false;

        for (Node local : localAdds) {
            if (local.equals(node) || node.equals(local)) return true;
            if (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == node.id()) return true;
            if (node instanceof ShadowUniverseNode && ((ShadowUniverseNode) node).id() == local.id()) return true;
        }

        if (node instanceof ShadowUniverseNode shadow) {
            if (shadow.id() >= 0 && transactionContext.getTombstonedNodeIds().get(shadow.id())) {
                return false;
            }
            return backingSet.contains(new UniverseNode(shadow.universe(), shadow.id()));
        }

        if (node.id() >= 0 && transactionContext.getTombstonedNodeIds().get(node.id())) {
             return false;
        }

        return backingSet.contains(node);
    }"""
content = re.sub(r'    @Override\n    public boolean contains\(Object obj\) \{.*?return backingSet\.contains\(node\);\n    \}', contains_node, content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

contains_edge = """    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) return false;

        for (Edge local : localAdds) {
            if (local.equals(edge) || edge.equals(local)) return true;
            if (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(edge)) return true;
            if (edge instanceof ShadowEdge && ((ShadowEdge) edge).backingEdge().equals(local)) return true;
        }

        if (edge instanceof ShadowEdge shadow) {
            if (shadow.id() >= 0 && transactionContext.getTombstonedEdgeIds().get(shadow.id())) {
                return false;
            }
            if (shadow.backingEdge() instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
                return backingSet.contains(new UniverseEdge(shadow.universe(), shadow.id()));
            }
            return false;
        }

        if (edge.id() >= 0 && transactionContext.getTombstonedEdgeIds().get(edge.id())) {
             return false;
        }

        return backingSet.contains(edge);
    }"""
content = re.sub(r'    @Override\n    public boolean contains\(Object obj\) \{.*?return backingSet\.contains\(edge\);\n    \}', contains_edge, content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)
