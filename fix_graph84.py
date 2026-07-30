# Ah! `Universe.promote(EphemeralGraph)` iterates over `ephemeralGraph.nodes()`.
# While iterating, it does: `this.addNodeTag(newId, tag);`
# `addNodeTag` modifies the Universe and calls `incrementModCount()`!
# Since `ephemeralGraph.nodes()` iterates over a `ShadowNodeSet`, which wraps a `UniverseNodeSet`.
# And `UniverseNodeSet` iterator checks `checkForComodification()`!
# `ShadowNodeSet.iterator()` calls `backingIter.hasNext()`!
# `backingIter` is `UniverseNodeSet$1` which fails because `universe.modCount()` changed!
# This is a ConcurrentModificationException because `addNodeTag` mutates the Universe!

# To fix this, `promote()` should materialize the `ephemeralGraph.nodes()` before iterating over it,
# OR `promote()` shouldn't mutate `Universe` until all nodes are read.
# Actually, since it's iterating over an EphemeralGraph, `ephemeralGraph.nodes().materialize()` returns a `GenericImmutableNodeSet` containing all the nodes, which avoids CME!

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/universe/Universe.java', 'r') as f:
    content = f.read()

content = content.replace("for (Node node : ephemeralGraph.nodes()) {", "for (Node node : ephemeralGraph.nodes().materialize()) {")
content = content.replace("for (Edge edge : ephemeralGraph.edges()) {", "for (Edge edge : ephemeralGraph.edges().materialize()) {")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/universe/Universe.java', 'w') as f:
    f.write(content)
