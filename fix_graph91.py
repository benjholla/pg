with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Graph intersection/union on EphemeralGraph uses retainAllEdges() and removeAllEdges() which we changed.
# retainAllEdges() removes edges NOT in `edgesToKeep`.
# Let's check retainAllEdges().
#
# public boolean retainAllEdges(Collection<? extends Edge> edgesToKeep) {
#     ...
#     for (Edge edge : this.edges.values()) {
#         if (!edgesToKeep.contains(edge)) {
#             toRemove.add(edge);
#         }
#     }
#     for (Edge edge : toRemove) {
#         result |= removeEdge(edge);
#     }
# }
# If `edgesToKeep` is a `ShadowEdgeSet` (which it is since it comes from another `EphemeralGraph.edges()`), does `contains(edge)` work?
# Yes, because we added the fallback loop in `ShadowEdgeSet.contains()`.
# Wait, `ShadowEdgeSet.contains()` uses `ShadowEdge.equals(edge)`.
# But `this.edges.values()` contains `ShadowEdge` and `EphemeralEdge`.
# So `edgesToKeep.contains(edge)` should work.
# Wait, does `removeEdge(edge)` properly remove it?
# In `EphemeralGraph.removeEdge(edge)`:
# if (edge instanceof ShadowEdge || edge instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
#     tombstonedEdgeIds.set(edgeId);
# }
# if (edges.remove(edgeId) == null && !tombstonedEdgeIds.get(edgeId)) { return false; }
# This means if it was a `ShadowEdge`, it is removed from `edges` and added to `tombstonedEdgeIds`.
# But wait, what if `removeEdge` was called, and it cascaded?
# `removeNode` cascades to `outEdges` and `inEdges` and calls `edges.remove()`, BUT it DOES NOT add to `tombstonedEdgeIds`!
