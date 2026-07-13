package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class EphemeralNodeSetCoverageTest {

    private EphemeralFactory factory;
    private EphemeralNode n1, n2, n3;

    @BeforeEach
    public void setUp() {
        factory = new EphemeralGraph().factory();
        n1 = (EphemeralNode) factory.createNode();
        n1.tags().add("test1");
        n1.tags().add("common");
        n1.attributes().put("attr1", "val1");
        n1.attributes().put("common_attr", "common_val");

        n2 = (EphemeralNode) factory.createNode();
        n2.tags().add("test2");
        n2.tags().add("common");
        n2.attributes().put("attr2", "val2");
        n2.attributes().put("common_attr", "common_val");

        n3 = (EphemeralNode) factory.createNode();
        n3.tags().add("test3");
    }

    @Test
    public void testConstructors() {
        EphemeralNodeSet empty = new EphemeralNodeSet();
        assertTrue(empty.isEmpty());

        EphemeralNodeSet single = new EphemeralNodeSet(n1);
        assertEquals(1, single.size());

        EphemeralNodeSet array = new EphemeralNodeSet(n1, n2);
        assertEquals(2, array.size());
        assertThrows(NullPointerException.class, () -> new EphemeralNodeSet((Node[]) null));

        EphemeralNodeSet collection = new EphemeralNodeSet(Arrays.asList(n1, n2, n3));
        assertEquals(3, collection.size());
        assertThrows(NullPointerException.class, () -> new EphemeralNodeSet((Collection<Node>) null));
    }

    @Test
    public void testValidate() {
        EphemeralNodeSet set = new EphemeralNodeSet();
        assertThrows(NullPointerException.class, () -> set.add(null));

        class InvalidNode implements Node {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        }
        assertThrows(IllegalArgumentException.class, () -> set.add(new InvalidNode()));
    }

    @Test
    public void testToImmutable() {
        EphemeralNodeSet emptySet = new EphemeralNodeSet();
        NodeSet emptyImmutable = emptySet.toImmutable();
        assertTrue(emptyImmutable.isEmpty());
        assertTrue(emptyImmutable instanceof dev.chpg.pg.api.NodeSet);

        EphemeralNodeSet singletonSet = new EphemeralNodeSet(n1);
        NodeSet singletonImmutable = singletonSet.toImmutable();
        assertEquals(1, singletonImmutable.size());
        assertTrue(singletonImmutable instanceof EphemeralImmutableSingletonNodeSet);

        EphemeralNodeSet multiSet = new EphemeralNodeSet(n1, n2);
        NodeSet multiImmutable = multiSet.toImmutable();
        assertEquals(2, multiImmutable.size());
        assertTrue(multiImmutable instanceof EphemeralImmutableNodeSet);
    }

    @Test
    public void testOne() {
        EphemeralNodeSet emptySet = new EphemeralNodeSet();
        assertFalse(emptySet.one().isPresent());

        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2);
        Optional<Node> one = set.one();
        assertTrue(one.isPresent());
        assertTrue(one.get() == n1 || one.get() == n2);
    }

    @Test
    public void testwithAttribute() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2, n3);

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
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2, n3);

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
        EphemeralNodeSet set1 = new EphemeralNodeSet(n1, n2);
        EphemeralNodeSet set2 = new EphemeralNodeSet(n2, n3);

        NodeSet intersectSet = set1.intersect(set2);
        assertEquals(1, intersectSet.size());
        assertTrue(intersectSet.contains(n2));

        NodeSet emptyIntersect = set1.intersect(new EphemeralNodeSet(n3));
        assertTrue(emptyIntersect.isEmpty());

        NodeSet nullIntersect = set1.intersect(null);
        assertTrue(nullIntersect.isEmpty());
    }

    @Test
    public void testDifference() {
        EphemeralNodeSet set1 = new EphemeralNodeSet(n1, n2);
        EphemeralNodeSet set2 = new EphemeralNodeSet(n2, n3);

        NodeSet diffSet = set1.difference(set2);
        assertEquals(1, diffSet.size());
        assertTrue(diffSet.contains(n1));

        NodeSet allDiff = set1.difference(new EphemeralNodeSet(n3));
        assertEquals(2, allDiff.size());
        assertTrue(allDiff.contains(n1));
        assertTrue(allDiff.contains(n2));

        NodeSet nullDiff = set1.difference(null);
        assertEquals(2, nullDiff.size());
    }

    @Test
    public void testUnion() {
        EphemeralNodeSet set1 = new EphemeralNodeSet(n1);
        EphemeralNodeSet set2 = new EphemeralNodeSet(n2, n3);

        NodeSet unionSet = set1.union(set2);
        assertEquals(3, unionSet.size());
        assertTrue(unionSet.contains(n1));
        assertTrue(unionSet.contains(n2));
        assertTrue(unionSet.contains(n3));

        NodeSet nullUnion = set1.union(null);
        assertEquals(1, nullUnion.size());
        assertTrue(nullUnion.contains(n1));

        class InvalidNode implements Node {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        }
        NodeSet mixUnion = set1.union(Arrays.asList(n2, new InvalidNode()));
        assertEquals(2, mixUnion.size());
        assertTrue(mixUnion.contains(n1));
        assertTrue(mixUnion.contains(n2));
    }

    @Test
    public void testIds() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2);
        Set<Integer> ids = set.ids();
        assertEquals(2, ids.size());
        assertTrue(ids.contains(n1.id()));
        assertTrue(ids.contains(n2.id()));
    }

    @Test
    public void testToIdArray() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2);
        int[] ids = set.toIdArray();
        assertEquals(2, ids.length);
        assertTrue(ids[0] == n1.id() || ids[0] == n2.id());
        assertTrue(ids[1] == n1.id() || ids[1] == n2.id());
    }

    @Test
    public void testContains() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1);
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
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2);
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
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2);
        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    public void testContainsAll() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2, n3);
        assertTrue(set.containsAll(Arrays.asList(n1, n2)));
        assertFalse(set.containsAll(Arrays.asList(n1, factory.createNode())));
        assertThrows(NullPointerException.class, () -> set.containsAll(null));
    }

    @Test
    public void testRetainAll() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2, n3);
        assertTrue(set.retainAll(Arrays.asList(n1, n2)));
        assertEquals(2, set.size());
        assertTrue(set.contains(n1));
        assertTrue(set.contains(n2));
        assertFalse(set.contains(n3));

        assertFalse(set.retainAll(Arrays.asList(n1, n2)));
        assertThrows(NullPointerException.class, () -> set.retainAll(null));
    }

    @Test
    public void testRemoveAll() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2, n3);
        assertTrue(set.removeAll(Arrays.asList(n1, n2)));
        assertEquals(1, set.size());
        assertTrue(set.contains(n3));

        assertFalse(set.removeAll(Arrays.asList(n1, n2)));
        assertThrows(NullPointerException.class, () -> set.removeAll(null));
    }

    @Test
    public void testwithAnyTag() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2, n3);

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
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2, n3);

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

    @Test
    public void testIteratorAndToArray() {
        EphemeralNodeSet set = new EphemeralNodeSet(n1, n2);

        int count = 0;
        Iterator<Node> it = set.iterator();
        while (it.hasNext()) {
            Node n = it.next();
            assertTrue(n == n1 || n == n2);
            count++;
        }
        assertEquals(2, count);

        Object[] objArray = set.toArray();
        assertEquals(2, objArray.length);

        Node[] nodeArray = set.toArray(new Node[0]);
        assertEquals(2, nodeArray.length);
    }

    @Test
    public void testToStringAndEqualsAndHashCode() {
        EphemeralNodeSet set1 = new EphemeralNodeSet(n1, n2);
        EphemeralNodeSet set2 = new EphemeralNodeSet(n2, n1);
        EphemeralNodeSet set3 = new EphemeralNodeSet(n1);

        assertTrue(set1.toString().contains(n1.toString()));
        assertTrue(set1.toString().contains(n2.toString()));
        assertTrue(set1.toString().startsWith("EphemeralNodeSet [nodes="));

        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());
        assertNotEquals(set1, set3);
        assertNotEquals(set1, "Not a set");
    }
}
