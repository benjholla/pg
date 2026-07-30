with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/universe/Universe.java', 'r') as f:
    content = f.read()

import re
print(re.search(r'ephemeralGraph\.clear\(\);', content, flags=re.DOTALL))
