with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

import re
content = content.replace("== 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);\n    }", "if (size() == 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);\n    }")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

content = content.replace("== 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);\n    }", "if (size() == 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);\n    }")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)
