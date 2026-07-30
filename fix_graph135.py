with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

import re
content = content.replace("if (local.equals(node)     return true;\n            if (node.equals(local)) return true;", "if (local.equals(node) || node.equals(local)) return true;")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

content = content.replace("if (local.equals(edge)     return true;\n            if (edge.equals(local)) return true;", "if (local.equals(edge) || edge.equals(local)) return true;")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)
