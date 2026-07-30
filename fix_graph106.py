with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java', 'r') as f:
    content = f.read()

import re
print(re.search(r'    public boolean contains\(Object obj\) \{.*?    \}', content, flags=re.DOTALL).group(0))
