with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

import re

# `if (size() == 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);\n    }\n    }`
# Let's fix the extra brace `}`
content = re.sub(r'return new dev\.chpg\.pg\.api\.GenericImmutableEdgeSet\(this\);\n    }\n    }', 'return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);\n    }', content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

content = re.sub(r'return new dev\.chpg\.pg\.api\.GenericImmutableNodeSet\(this\);\n    }\n    }', 'return new dev.chpg.pg.api.GenericImmutableNodeSet(this);\n    }', content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)
