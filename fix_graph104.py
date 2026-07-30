import re

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Remove the GlobalNode / GlobalEdge check from EphemeralGraph, because pg-multiverse shouldn't depend on pg-global
content = content.replace("if (edge instanceof dev.chpg.pg.global.GlobalEdge) {\n                     return false; // Silently ignore global edge\n                 }", "")
content = content.replace("if (node instanceof dev.chpg.pg.global.GlobalNode) {\n                     return false; // Silently ignore global node\n                 }", "")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
