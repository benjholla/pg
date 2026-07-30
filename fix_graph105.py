with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Let's check `edges(Node source, Node target)` in EphemeralGraph.
# It uses `getOutEdgesFromNode(source)` which returns `ShadowEdgeSet`!
# `getOutEdgesFromNode` is what we implemented using ShadowEdgeSet!
# Wait, `getOutEdgesFromNode` adds to `pureEphemeralAdds`.
# But `EphemeralGraph.edges(source, target)` constructs a NEW `EphemeralEdgeSet` with the results:
#
# Optional<EdgeSet> out = getOutEdgesFromNode(eSource);
# EdgeSet result = new EphemeralEdgeSet();
# for (Edge e : out.get()) {
#     if (e.to().equals(eTarget)) { result.add(e); }
# }
# return result;
#
# BUT! `e1` is an `EphemeralEdge`.
# `out.get()` yields `ShadowEdge`. `result.add(e)` adds `ShadowEdge` to `EphemeralEdgeSet`.
# And `graph.edges(a, b).contains(e1)` checks `EphemeralEdgeSet.contains(e1)`.
# Since `e1` is an `EphemeralEdge`, and `result` contains `ShadowEdge`, `contains` fails!
# We just need to make `EphemeralEdgeSet.contains()` check unwrapping correctly?
# Wait, I previously changed `EphemeralEdgeSet.contains` to iterate over its elements! Let's verify `EphemeralEdgeSet.contains`.
