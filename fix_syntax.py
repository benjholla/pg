import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Revert bad replacement for if (obj == null || getClass() != obj.getClass()) return false;
    content = content.replace("if (obj == null || getClass() { != obj.getClass()) return false; }", "if (obj == null || getClass() != obj.getClass()) { return false; }")

    # Check for other potential bad regex replacements
    # The regex was `if\s*\((.*?)\)\s*([^{}\n]*);` -> `if (\1) { \2; }`
    # It might have broken `if (attrVal instanceof String) attributes.put(attrKey, (String) attrVal);` -> `if (attrVal instanceof String) { attributes.put(attrKey, (String) attrVal); }` -> this is fine.

    with open(filepath, 'w') as f:
        f.write(content)

fix_file('pg-api/src/test/java/dev/chpg/pg/api/DeferredEdgeSetTest.java')
fix_file('pg-api/src/test/java/dev/chpg/pg/api/DeferredNodeSetTest.java')
