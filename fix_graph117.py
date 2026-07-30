import re
import glob

# Remove Duplicate imports
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeIterator.java', 'r') as f:
    content = f.read()
content = content.replace("import dev.chpg.pg.multiverse.universe.UniverseNode;\n", "")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeIterator.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSet.java', 'r') as f:
    content = f.read()
content = content.replace("import dev.chpg.pg.api.NodeSet;\nimport dev.chpg.pg.api.Node;", "import dev.chpg.pg.api.NodeSet;")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSet.java', 'r') as f:
    content = f.read()
content = content.replace("import dev.chpg.pg.api.EdgeSet;\nimport dev.chpg.pg.api.Node;\nimport dev.chpg.pg.api.Edge;", "import dev.chpg.pg.api.EdgeSet;\nimport dev.chpg.pg.api.Node;")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSet.java', 'w') as f:
    f.write(content)

# Add braces for single line ifs
def add_braces(match):
    return match.group(1) + " {\n    " + match.group(2) + "\n}"

for file in glob.glob('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/**/*.java', recursive=True):
    with open(file, 'r') as f:
        content = f.read()

    # Simple regex to catch `if (cond) stmt;`
    content = re.sub(r'(if\s*\([^)]+\))\s+([^{}\n;]+;)', add_braces, content)

    with open(file, 'w') as f:
        f.write(content)
