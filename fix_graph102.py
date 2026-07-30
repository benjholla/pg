# The user's prompt suggested:
# `return this.edges.remove(edgeId) != null;` for `edgeId < 0`.
# BUT wait! `ShadowEdge` objects wrap `EphemeralEdge` and `ShadowEdge.id()` delegates to `backingEdge.id()`.
# So a `ShadowEdge` wrapping an `EphemeralEdge` HAS A NEGATIVE ID!
# So `edgeId < 0` IS STILL an `EphemeralEdge` (which was shielded when created, maybe from another graph?)
# Actually, the user's explicit code for `removeEdge`:
# ```java
#     public boolean removeEdge(Edge edge) {
#         int edgeId = edge.id();
#
#         // 1. Ephemeral Lineage (Negative ID)
#         if (edgeId < 0) {
#             return this.edges.remove(edgeId) != null;
#         }
#         ...
# ```
# Did I implement it correctly?
# Yes, but wait! The user's code for `removeEdge(Edge edge)` DID NOT have the cascade disconnects!
# Oh!!
# ```java
#         // 2. Disconnect from pillars
#         EphemeralEdgeSet fromOut = outEdges.get(fromId);
#         if (fromOut != null) {
#             fromOut.remove(edge);
#         }
#         EphemeralEdgeSet toIn = inEdges.get(toId);
#         if (toIn != null) {
#             toIn.remove(edge);
#         }
# ```
# If I just return `this.edges.remove(edgeId) != null`, the pillars (`outEdges` and `inEdges`) STILL retain the edge!
# That breaks graph invariants! `GraphDegreeInvariantTest` would fail! Wait, `testRetainAllEdges` fails because it retains edges but the degree says something else? No, `testRetainAllEdges` fails on `assertTrue(changed)` when it calls `retainAllEdges`.
