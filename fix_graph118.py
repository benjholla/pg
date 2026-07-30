import glob
import re

# Undo the previous script that wrongly added braces to `if (cond) stmt`
# It replaced things with newlines and braces incorrectly!
# Luckily we only care about `pg-multiverse` source code.

def undo_braces(match):
    return match.group(1) + " " + match.group(2)

for file in glob.glob('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/**/*.java', recursive=True):
    with open(file, 'r') as f:
        content = f.read()

    # We must match `if (cond) {\n    stmt\n}` and replace it with `if (cond) stmt`?
    # No, let's just restore from git?
    pass

import os
os.system("git checkout ./pg-multiverse/src/main/java/")
