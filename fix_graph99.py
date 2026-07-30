with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

import re

# `retainAllEdges` expects `edgesToKeep.contains(edge)` to work.
# `this.edges.values()` contains `ShadowEdge` objects.
# But `Arrays.asList(e1)` contains `EphemeralEdge`.
# So `edgesToKeep.contains(edge)` is `Arrays.asList(e1).contains(ShadowEdge)`, which evaluates `ShadowEdge.equals(EphemeralEdge)`.
# `ShadowEdge.equals(EphemeralEdge)` uses `backingEdge.equals()`, which returns `EphemeralEdge.equals(EphemeralEdge)`, which returns `true`!
# Wait! Does `Arrays.asList` do `ShadowEdge.equals(EphemeralEdge)`?
# `ArrayList.contains(o)` calls `o.equals(element)`!
# Wait, `o` is `ShadowEdge`. `element` is `EphemeralEdge`.
# `ShadowEdge.equals(EphemeralEdge)` returns `true`.
# BUT wait! `retainAllEdges` code:
# Collection<Edge> toRemove = new ArrayList<>();
# for (Edge edge : this.edges.values()) {
#     if (!edgesToKeep.contains(edge)) {
#         toRemove.add(edge);
#     }
# }
# If `edgesToKeep.contains(edge)` is `true`, it is NOT removed.
# But what if `edgesToKeep.contains(edge)` is FALSE? Then it removes `e2`.
# Wait, why did it say `changed` is false?
# Ah! `removeEdge(edge)` returned false!
# Let's check `removeEdge(edge)` in EphemeralGraph.
