with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java', 'r') as f:
    content = f.read()

import re

contains_fix = """    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) { return false; }
        for (Edge local : internalSet) {
            if (local.equals(edge) || edge.equals(local)) return true;
            if (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(edge)) return true;
            if (edge instanceof ShadowEdge && ((ShadowEdge) edge).backingEdge().equals(local)) return true;
        }
        return false;
    }"""
content = re.sub(r'    @Override\n    public boolean contains\(Object obj\) \{.*?(?=    @Override\n    public boolean remove)', contains_fix + "\n", content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'r') as f:
    content = f.read()

contains_fix_node = """    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Node node)) { return false; }
        for (Node local : internalSet) {
            if (local.equals(node) || node.equals(local)) return true;
            if (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == node.id()) return true;
            if (node instanceof ShadowUniverseNode && ((ShadowUniverseNode) node).id() == local.id()) return true;
        }
        return false;
    }"""
content = re.sub(r'    @Override\n    public boolean contains\(Object obj\) \{.*?(?=    @Override\n    public boolean remove)', contains_fix_node + "\n", content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'w') as f:
    f.write(content)
