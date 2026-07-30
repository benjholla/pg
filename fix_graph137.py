with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()
import re
content = re.sub(r'== 1\) \{\n            return new dev\.chpg\.pg\.api\.GenericImmutableNodeSet\(java\.util\.Collections\.singleton\(one\(\)\.get\(\)\)\);\n        \}\n        return new dev\.chpg\.pg\.api\.GenericImmutableNodeSet\(this\);\n    \}', '== 1) {\n            return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));\n        }\n        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);\n    }', content)
# Check what the actual problem is:
lines = content.split('\n')
for i, line in enumerate(lines[365:]):
    print(f"{i+365}: {line}")
