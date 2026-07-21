import os
import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Fix NeedBraces for 'if' statements
    content = re.sub(r'if\s*\((.*?)\)\s*([^{}\n]*);', r'if (\1) { \2; }', content)

    # Fix unused imports
    content = re.sub(r'import static org.junit.jupiter.api.Assertions.assertArrayEquals;\n', '', content)
    content = re.sub(r'import java.util.Collections;\n', '', content)

    # Ensure NewlineAtEndOfFile
    if not content.endswith('\n'):
        content += '\n'

    with open(filepath, 'w') as f:
        f.write(content)

fix_file('pg-api/src/test/java/dev/chpg/pg/api/DeferredEdgeSetTest.java')
fix_file('pg-api/src/test/java/dev/chpg/pg/api/DeferredNodeSetTest.java')
fix_file('pg-api/src/test/java/dev/chpg/pg/api/AttributeMapTest.java')
fix_file('pg-api/src/test/java/dev/chpg/pg/api/NodeDirectionTest.java')
fix_file('pg-api/src/test/java/dev/chpg/pg/api/AttributeValueTest.java')
