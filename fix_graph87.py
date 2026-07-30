with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

# Let's fix `ShadowEdgeSet.intersect` which might be incorrectly doing difference instead of retainAll for `tombstonedEdgeIds`?
# In `intersect`:
# `filteredIntersect = filteredUniverseIntersect.difference(new UniverseEdgeSet(..., getTombstonedEdgeIds()))`
# Wait, if `other` is `UniverseEdgeSet`, `filteredUniverseIntersect` drops tombstones correctly.
# What about `combinedLocalAdds`?
# `Set<Edge> combinedLocalAdds = new HashSet<>(this.localAdds); combinedLocalAdds.retainAll(other);`
# But wait! If `other` is a `ShadowEdgeSet`, it contains `ShadowEdge` objects. `combinedLocalAdds` contains `EphemeralEdge` objects inside `EphemeralGraph.edges()`.
# When we do `combinedLocalAdds.retainAll(other)`, if `other` contains `ShadowEdge(ephemeralEdge)`, does `EphemeralEdge.equals(ShadowEdge)` work? NO!
# `EphemeralEdge.equals(ShadowEdge)` is false! So `combinedLocalAdds` becomes empty!
# Same problem as `contains`!
# We need `ShadowNodeSet` and `ShadowEdgeSet` to properly do algebra over localAdds taking into account wrappers!
