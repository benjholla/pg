import re
with open('./pg-evaluation/src/test/java/dev/chpg/pg/evaluation/CrossGraphContaminationTest.java', 'r') as f:
    content = f.read()

# Make sure they all share the same Universe in a method scope if applicable, but for CrossGraphContaminationTest, it creates multiple graphs. Wait, it might be fine, but if we need to share universes, let's look at it.
pass
