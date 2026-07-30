with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Let's fix EphemeralGraph.edges(Node source, Node target)
# In my previous fix:
# EdgeSet result = new EphemeralEdgeSet();
# for (Edge e : out.get()) {
#     if (e.to().equals(target) || e.to().id() == target.id()) {
#         result.add(e);
#     }
# }
# Wait! `result` is `EphemeralEdgeSet`. `e` might be `ShadowEdge`!
# `EphemeralEdgeSet.add()` will throw `IllegalArgumentException`?
# NO, we patched `validate(e)` to accept `ShadowEdge`. But wait, in `contains` we fixed `ShadowEdgeSet` to intercept.
# Wait, `assertTrue(graph.edges(a, b).contains(e1))` fails!
# Because `graph.edges(a, b)` returns `GenericImmutableEdgeSet(result)`.
# `GenericImmutableEdgeSet.contains(e1)` checks `result.contains(e1)`.
# Oh! `GenericImmutableEdgeSet` wraps the collection exactly.
# It does `internalSet.contains(e1)`.
# If `internalSet` contains `ShadowEdge(e1)`, does `ShadowEdge(e1).equals(e1)`? YES.
# BUT does `e1.equals(ShadowEdge)`? NO!
# And `HashSet.contains()` might evaluate `e1.equals(ShadowEdge)`.
# In `GenericImmutableEdgeSet`, the set is backed by `java.util.Collections.unmodifiableSet(internalSet)`.
# So we need to make sure the set contains the `backingEdge` if it's `ShadowEdge(EphemeralEdge)`!
