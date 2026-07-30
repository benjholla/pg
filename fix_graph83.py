with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/universe/Universe.java', 'r') as f:
    content = f.read()

import re
print("promote:")
print(re.search(r'    public UniverseGraph promote\(EphemeralGraph ephemeralGraph\) \{.*?    \}', content, flags=re.DOTALL).group(0))
