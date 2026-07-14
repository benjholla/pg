package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.api.Graph;

public class GlobalNodeSetTest {

    private GlobalNode n1, n2, n3;

    @BeforeEach
    public void setUp() {
        n1 = new GlobalNode();
        n1.tags().add("test1");
        n1.tags().add("common");
        n1.attributes().put("attr1", "val1");
        n1.attributes().put("common_attr", "common_val");

        n2 = new GlobalNode();
        n2.tags().add("test2");
        n2.tags().add("common");
        n2.attributes().put("attr2", "val2");
        n2.attributes().put("common_attr", "common_val");

        n3 = new GlobalNode();
        n3.tags().add("test3");
    }

    @Test
    public void testValidate() {
        GlobalNodeSet set = new GlobalNodeSet();
        assertThrows(NullPointerException.class, () -> set.add(null));

        // Mocking an invalid node type using a simple local class
        class InvalidNode implements Node {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        }
        assertThrows(IllegalArgumentException.class, () -> set.add(new InvalidNode()));
    }

    @Test
    public void testToImmutable() {
        GlobalNodeSet emptySet = new GlobalNodeSet();
        NodeSet emptyImmutable = emptySet.toImmutable();
        assertTrue(emptyImmutable.isEmpty());
        assertTrue(emptyImmutable instanceof dev.chpg.pg.api.NodeSet);

        GlobalNodeSet singletonSet = new GlobalNodeSet(n1);
        NodeSet singletonImmutable = singletonSet.toImmutable();
        assertEquals(1, singletonImmutable.size());
        assertTrue(singletonImmutable instanceof GlobalImmutableSingletonNodeSet);

        GlobalNodeSet multiSet = new GlobalNodeSet(n1, n2);
        NodeSet multiImmutable = multiSet.toImmutable();
        assertEquals(2, multiImmutable.size());
        assertTrue(multiImmutable instanceof GlobalImmutableNodeSet);
    }

    @Test
    public void testwithAttribute() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2, n3);

        NodeSet attr1Set = set.withAttribute("attr1");
        assertEquals(1, attr1Set.size());
        assertTrue(attr1Set.contains(n1));

        NodeSet commonSet = set.withAttribute("common_attr");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(n1));
        assertTrue(commonSet.contains(n2));

        NodeSet missingSet = set.withAttribute("missing_attr");
        assertTrue(missingSet.isEmpty());
    }

    @Test
    public void testFilterWithValues() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2, n3);

        NodeSet val1Set = set.withAttribute("attr1", AttributeValue.value("val1"));
        assertEquals(1, val1Set.size());
        assertTrue(val1Set.contains(n1));

        NodeSet val2Set = set.withAttribute("attr2", AttributeValue.value("val2"), AttributeValue.value("other"));
        assertEquals(1, val2Set.size());
        assertTrue(val2Set.contains(n2));

        NodeSet missingSet = set.withAttribute("attr1", AttributeValue.value("missing"));
        assertTrue(missingSet.isEmpty());

        NodeSet nullAttrSet = set.withAttribute(null, AttributeValue.value("val1"));
        assertTrue(nullAttrSet.isEmpty());

        NodeSet nullValsSet = set.withAttribute("attr1", (AttributeValue[]) null);
        assertTrue(nullValsSet.isEmpty());

        NodeSet withNullValSet = set.withAttribute("attr1", new AttributeValue[]{null});
        assertTrue(withNullValSet.isEmpty());
    }

    @Test
    public void testIntersect() {
        GlobalNodeSet set1 = new GlobalNodeSet(n1, n2);
        GlobalNodeSet set2 = new GlobalNodeSet(n2, n3);

        NodeSet intersectSet = set1.intersect(set2);
        assertEquals(1, intersectSet.size());
        assertTrue(intersectSet.contains(n2));

        NodeSet emptyIntersect = set1.intersect(new GlobalNodeSet(n3));
        assertTrue(emptyIntersect.isEmpty());

        NodeSet nullIntersect = set1.intersect(null);
        assertTrue(nullIntersect.isEmpty());
    }

    @Test
    public void testDifference() {
        GlobalNodeSet set1 = new GlobalNodeSet(n1, n2);
        GlobalNodeSet set2 = new GlobalNodeSet(n2, n3);

        NodeSet diffSet = set1.difference(set2);
        assertEquals(1, diffSet.size());
        assertTrue(diffSet.contains(n1));

        NodeSet allDiff = set1.difference(new GlobalNodeSet(n3));
        assertEquals(2, allDiff.size());
        assertTrue(allDiff.contains(n1));
        assertTrue(allDiff.contains(n2));

        NodeSet nullDiff = set1.difference(null);
        assertEquals(2, nullDiff.size());
    }

    @Test
    public void testUnion() {
        GlobalNodeSet set1 = new GlobalNodeSet(n1);
        GlobalNodeSet set2 = new GlobalNodeSet(n2, n3);

        NodeSet unionSet = set1.union(set2);
        assertEquals(3, unionSet.size());
        assertTrue(unionSet.contains(n1));
        assertTrue(unionSet.contains(n2));
        assertTrue(unionSet.contains(n3));

        NodeSet nullUnion = set1.union(null);
        assertEquals(1, nullUnion.size());
        assertTrue(nullUnion.contains(n1));

        // Test with a generic collection containing different types of Nodes
        class InvalidNode implements Node {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        }
        NodeSet mixUnion = set1.union(Arrays.asList(n2, new InvalidNode()));
        assertEquals(2, mixUnion.size()); // InvalidNode should be ignored
        assertTrue(mixUnion.contains(n1));
        assertTrue(mixUnion.contains(n2));
    }

    @Test
    public void testIds() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2);
        Set<Integer> ids = set.ids();
        assertEquals(2, ids.size());
        assertTrue(ids.contains(n1.id()));
        assertTrue(ids.contains(n2.id()));
    }

    @Test
    public void testToIdArray() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2);
        int[] ids = set.toIdArray();
        assertEquals(2, ids.length);
        // Array order is not guaranteed, but both IDs should be present
        assertTrue(ids[0] == n1.id() || ids[0] == n2.id());
        assertTrue(ids[1] == n1.id() || ids[1] == n2.id());
    }

    @Test
    public void testContains() {
        GlobalNodeSet set = new GlobalNodeSet(n1);
        assertTrue(set.contains(n1));
        assertFalse(set.contains(n2));
        assertFalse(set.contains(null));
        assertFalse(set.contains("Not a Node"));

        class InvalidNode implements Node {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        }
        assertFalse(set.contains(new InvalidNode()));
    }

    @Test
    public void testRemove() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2);
        assertTrue(set.remove(n1));
        assertFalse(set.contains(n1));
        assertEquals(1, set.size());

        assertFalse(set.remove(n3));
        assertFalse(set.remove(null));
        assertFalse(set.remove("Not a Node"));

        class InvalidNode implements Node {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        }
        assertFalse(set.remove(new InvalidNode()));
    }

    @Test
    public void testClear() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2);
        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    public void testContainsAll() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2, n3);
        assertTrue(set.containsAll(Arrays.asList(n1, n2)));
        assertFalse(set.containsAll(Arrays.asList(n1, new GlobalNode()))); // New node not in set
        assertThrows(NullPointerException.class, () -> set.containsAll(null));
    }

    @Test
    public void testRetainAll() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2, n3);
        assertTrue(set.retainAll(Arrays.asList(n1, n2)));
        assertEquals(2, set.size());
        assertTrue(set.contains(n1));
        assertTrue(set.contains(n2));
        assertFalse(set.contains(n3));

        assertFalse(set.retainAll(Arrays.asList(n1, n2))); // No change
        assertThrows(NullPointerException.class, () -> set.retainAll(null));
    }

    @Test
    public void testRemoveAll() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2, n3);
        assertTrue(set.removeAll(Arrays.asList(n1, n2)));
        assertEquals(1, set.size());
        assertTrue(set.contains(n3));

        assertFalse(set.removeAll(Arrays.asList(n1, n2))); // No change
        assertThrows(NullPointerException.class, () -> set.removeAll(null));
    }

    @Test
    public void testwithAnyTag() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2, n3);

        NodeSet test1Set = set.withAnyTag("test1");
        assertEquals(1, test1Set.size());
        assertTrue(test1Set.contains(n1));

        NodeSet commonSet = set.withAnyTag("common");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(n1));
        assertTrue(commonSet.contains(n2));

        NodeSet multiTagSet = set.withAnyTag("test1", "test3");
        assertEquals(2, multiTagSet.size());
        assertTrue(multiTagSet.contains(n1));
        assertTrue(multiTagSet.contains(n3));

        NodeSet missingSet = set.withAnyTag("missing");
        assertTrue(missingSet.isEmpty());

        NodeSet nullTagsSet = set.withAnyTag((String[]) null);
        assertTrue(nullTagsSet.isEmpty());

        NodeSet emptyTagsSet = set.withAnyTag();
        assertTrue(emptyTagsSet.isEmpty());
    }

    @Test
    public void testwithAllTags() {
        GlobalNodeSet set = new GlobalNodeSet(n1, n2, n3);

        NodeSet test1Set = set.withAllTags("test1");
        assertEquals(1, test1Set.size());
        assertTrue(test1Set.contains(n1));

        NodeSet commonSet = set.withAllTags("common");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(n1));
        assertTrue(commonSet.contains(n2));

        NodeSet bothSet = set.withAllTags("test1", "common");
        assertEquals(1, bothSet.size());
        assertTrue(bothSet.contains(n1));

        NodeSet missingSet = set.withAllTags("test1", "missing");
        assertTrue(missingSet.isEmpty());

        NodeSet nullTagsSet = set.withAllTags((String[]) null);
        assertTrue(nullTagsSet.isEmpty());

        NodeSet emptyTagsSet = set.withAllTags();
        assertTrue(emptyTagsSet.isEmpty());
    }
}
