with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Ah! `clear()` also needs to clear `tombstonedNodeIds` and `tombstonedEdgeIds`.
# But wait! If `clear()` clears nodes and edges, what about `baseline` inside `nodes()`?
# `baseline` comes from `universe.activeNodeIds()`.
# Wait! Does `clear()` on `EphemeralGraph` mean it clears its LOCAL nodes, OR DOES IT CLEAR THE UNIVERSE?
# "clear() ... permanently invalidates the ephemeral sandbox"
# Wait! In `EphemeralGraph.nodes()`:
# `NodeSet baseline = new dev.chpg.pg.multiverse.universe.UniverseNodeSet(...)`
# If we clear `EphemeralGraph`, it shouldn't show `UniverseNodes`? No, if we call `eg.nodes()`, and it's cleared, it shouldn't show ANY nodes!
# But `nodes()` STILL returns `baseline` which has `activeNodeIds()`!
# We need to drop tombstones? Or add a boolean `cleared = false`?
