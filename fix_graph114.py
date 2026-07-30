with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

import re
print(re.search(r'    @Override\n    public void clear\(\) \{.*?    \}', content, flags=re.DOTALL).group(0))
