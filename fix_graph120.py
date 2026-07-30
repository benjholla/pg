with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

import re
content = content.replace("if (local.equals(node)", "if (local.equals(node))")
content = content.replace("|| node.equals(local)) return true;", "    return true;\nif (node.equals(local)) return true;")
content = content.replace("if (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) ", "if (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) ")
content = content.replace("local).id() == node.id()) return true;", "local).id() == node.id()) return true;")
content = content.replace("if (node instanceof ShadowUniverseNode && ((ShadowUniverseNode) ", "if (node instanceof ShadowUniverseNode && ((ShadowUniverseNode) ")
content = content.replace("node).id() == local.id()) return true;", "node).id() == local.id()) return true;")

content = content.replace("if (size() ", "if (size() ")
content = content.replace("== 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));", "== 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

content = content.replace("if (local.equals(edge)", "if (local.equals(edge))")
content = content.replace("|| edge.equals(local)) return true;", "    return true;\nif (edge.equals(local)) return true;")
content = content.replace("if (local instanceof ShadowEdge && ((ShadowEdge) ", "if (local instanceof ShadowEdge && ((ShadowEdge) ")
content = content.replace("local).backingEdge().equals(edge)) return true;", "local).backingEdge().equals(edge)) return true;")
content = content.replace("if (edge instanceof ShadowEdge && ((ShadowEdge) ", "if (edge instanceof ShadowEdge && ((ShadowEdge) ")
content = content.replace("edge).backingEdge().equals(local)) return true;", "edge).backingEdge().equals(local)) return true;")

content = content.replace("if (size() ", "if (size() ")
content = content.replace("== 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));", "== 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)
