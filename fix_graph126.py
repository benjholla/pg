with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

import re
content = content.replace("if (local.equals(node)     return true;\n            if (node.equals(local)) return true;", "if (local.equals(node) || node.equals(local)) return true;")
content = content.replace("== 1) {\n            return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));\n        }\n        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);", "== 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

content = content.replace("if (local.equals(edge)     return true;\n            if (edge.equals(local)) return true;", "if (local.equals(edge) || edge.equals(local)) return true;")
content = content.replace("== 1) {\n            return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        }\n        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);", "== 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)
