# Ah! `Universe.promote(EphemeralGraph)` iterates over `ephemeralGraph.nodes()`.
# Inside the loop:
# `int newId = this.idGenerator.createNodeId();`
# Wait! Does `idGenerator.createNodeId()` update `universeModCount`?
# Let's check `UniverseIdGenerator.java`.
# Yes! Maybe it increments `universeModCount`!
# Oh, `createNodeId()` increments `nextNodeId`. Wait, does `universeModCount` change?
