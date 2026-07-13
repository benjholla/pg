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
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;

public class GlobalEdgeSetTest {

    private GlobalEdge e1, e2, e3;
    private GlobalNode n1, n2;

    @BeforeEach
    public void setUp() {
        n1 = new GlobalNode();
        n2 = new GlobalNode();

        e1 = new GlobalEdge(n1, n2);
        e1.tags().add("test1");
        e1.tags().add("common");
        e1.attributes().put("attr1", "val1");
        e1.attributes().put("common_attr", "common_val");

        e2 = new GlobalEdge(n2, n1);
        e2.tags().add("test2");
        e2.tags().add("common");
        e2.attributes().put("attr2", "val2");
        e2.attributes().put("common_attr", "common_val");

        e3 = new GlobalEdge(n1, n1);
        e3.tags().add("test3");
    }

    @Test
    public void testConstructors() {
        GlobalEdgeSet empty = new GlobalEdgeSet();
        assertTrue(empty.isEmpty());

        GlobalEdgeSet single = new GlobalEdgeSet(e1);
        assertEquals(1, single.size());

        GlobalEdgeSet array = new GlobalEdgeSet(e1, e2);
        assertEquals(2, array.size());
        assertThrows(NullPointerException.class, () -> new GlobalEdgeSet((Edge[]) null));

        GlobalEdgeSet collection = new GlobalEdgeSet(Arrays.asList(e1, e2, e3));
        assertEquals(3, collection.size());
        assertThrows(NullPointerException.class, () -> new GlobalEdgeSet((Collection<Edge>) null));
    }

    @Test
    public void testValidate() {
        GlobalEdgeSet set = new GlobalEdgeSet();
        assertThrows(NullPointerException.class, () -> set.add(null));

        // Mocking an invalid edge type using a simple local class
        class InvalidEdge implements Edge {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            @Override public Node from() { return null; }
            @Override public Node to() { return null; }
        }
        assertThrows(IllegalArgumentException.class, () -> set.add(new InvalidEdge()));
    }

    @Test
    public void testToImmutable() {
        GlobalEdgeSet emptySet = new GlobalEdgeSet();
        EdgeSet emptyImmutable = emptySet.toImmutable();
        assertTrue(emptyImmutable.isEmpty());
        assertTrue(emptyImmutable instanceof dev.chpg.pg.api.EdgeSet);

        GlobalEdgeSet singletonSet = new GlobalEdgeSet(e1);
        EdgeSet singletonImmutable = singletonSet.toImmutable();
        assertEquals(1, singletonImmutable.size());
        assertTrue(singletonImmutable instanceof GlobalImmutableSingletonEdgeSet);

        GlobalEdgeSet multiSet = new GlobalEdgeSet(e1, e2);
        EdgeSet multiImmutable = multiSet.toImmutable();
        assertEquals(2, multiImmutable.size());
        assertTrue(multiImmutable instanceof GlobalImmutableEdgeSet);
    }

    @Test
    public void testOne() {
        GlobalEdgeSet emptySet = new GlobalEdgeSet();
        assertFalse(emptySet.one().isPresent());

        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2);
        Optional<Edge> one = set.one();
        assertTrue(one.isPresent());
        assertTrue(one.get() == e1 || one.get() == e2);
    }

    @Test
    public void testFilter() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2, e3);

        EdgeSet attr1Set = set.filter("attr1");
        assertEquals(1, attr1Set.size());
        assertTrue(attr1Set.contains(e1));

        EdgeSet commonSet = set.filter("common_attr");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(e1));
        assertTrue(commonSet.contains(e2));

        EdgeSet missingSet = set.filter("missing_attr");
        assertTrue(missingSet.isEmpty());
    }

    @Test
    public void testFilterWithValues() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2, e3);

        EdgeSet val1Set = set.filter("attr1", AttributeValue.value("val1"));
        assertEquals(1, val1Set.size());
        assertTrue(val1Set.contains(e1));

        EdgeSet val2Set = set.filter("attr2", AttributeValue.value("val2"), AttributeValue.value("other"));
        assertEquals(1, val2Set.size());
        assertTrue(val2Set.contains(e2));

        EdgeSet missingSet = set.filter("attr1", AttributeValue.value("missing"));
        assertTrue(missingSet.isEmpty());

        EdgeSet nullAttrSet = set.filter(null, AttributeValue.value("val1"));
        assertTrue(nullAttrSet.isEmpty());

        EdgeSet nullValsSet = set.filter("attr1", (AttributeValue[]) null);
        assertTrue(nullValsSet.isEmpty());

        EdgeSet withNullValSet = set.filter("attr1", new AttributeValue[]{null});
        assertTrue(withNullValSet.isEmpty());
    }

    @Test
    public void testIntersect() {
        GlobalEdgeSet set1 = new GlobalEdgeSet(e1, e2);
        GlobalEdgeSet set2 = new GlobalEdgeSet(e2, e3);

        EdgeSet intersectSet = set1.intersect(set2);
        assertEquals(1, intersectSet.size());
        assertTrue(intersectSet.contains(e2));

        EdgeSet emptyIntersect = set1.intersect(new GlobalEdgeSet(e3));
        assertTrue(emptyIntersect.isEmpty());

        EdgeSet nullIntersect = set1.intersect(null);
        assertTrue(nullIntersect.isEmpty());
    }

    @Test
    public void testDifference() {
        GlobalEdgeSet set1 = new GlobalEdgeSet(e1, e2);
        GlobalEdgeSet set2 = new GlobalEdgeSet(e2, e3);

        EdgeSet diffSet = set1.difference(set2);
        assertEquals(1, diffSet.size());
        assertTrue(diffSet.contains(e1));

        EdgeSet allDiff = set1.difference(new GlobalEdgeSet(e3));
        assertEquals(2, allDiff.size());
        assertTrue(allDiff.contains(e1));
        assertTrue(allDiff.contains(e2));

        EdgeSet nullDiff = set1.difference(null);
        assertEquals(2, nullDiff.size());
    }

    @Test
    public void testUnion() {
        GlobalEdgeSet set1 = new GlobalEdgeSet(e1);
        GlobalEdgeSet set2 = new GlobalEdgeSet(e2, e3);

        EdgeSet unionSet = set1.union(set2);
        assertEquals(3, unionSet.size());
        assertTrue(unionSet.contains(e1));
        assertTrue(unionSet.contains(e2));
        assertTrue(unionSet.contains(e3));

        EdgeSet nullUnion = set1.union(null);
        assertEquals(1, nullUnion.size());
        assertTrue(nullUnion.contains(e1));

        // Test with a generic collection containing different types of Edges
        class InvalidEdge implements Edge {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            @Override public Node from() { return null; }
            @Override public Node to() { return null; }
        }
        EdgeSet mixUnion = set1.union(Arrays.asList(e2, new InvalidEdge()));
        assertEquals(2, mixUnion.size()); // InvalidEdge should be ignored
        assertTrue(mixUnion.contains(e1));
        assertTrue(mixUnion.contains(e2));
    }

    @Test
    public void testIds() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2);
        Set<Integer> ids = set.ids();
        assertEquals(2, ids.size());
        assertTrue(ids.contains(e1.id()));
        assertTrue(ids.contains(e2.id()));
    }

    @Test
    public void testToIdArray() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2);
        int[] ids = set.toIdArray();
        assertEquals(2, ids.length);
        // Array order is not guaranteed, but both IDs should be present
        assertTrue(ids[0] == e1.id() || ids[0] == e2.id());
        assertTrue(ids[1] == e1.id() || ids[1] == e2.id());
    }

    @Test
    public void testContains() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1);
        assertTrue(set.contains(e1));
        assertFalse(set.contains(e2));
        assertFalse(set.contains(null));
        assertFalse(set.contains("Not an Edge"));

        class InvalidEdge implements Edge {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            @Override public Node from() { return null; }
            @Override public Node to() { return null; }
        }
        assertFalse(set.contains(new InvalidEdge()));
    }

    @Test
    public void testRemove() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2);
        assertTrue(set.remove(e1));
        assertFalse(set.contains(e1));
        assertEquals(1, set.size());

        assertFalse(set.remove(e3));
        assertFalse(set.remove(null));
        assertFalse(set.remove("Not an Edge"));

        class InvalidEdge implements Edge {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            @Override public Node from() { return null; }
            @Override public Node to() { return null; }
        }
        assertFalse(set.remove(new InvalidEdge()));
    }

    @Test
    public void testClear() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2);
        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    public void testContainsAll() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2, e3);
        assertTrue(set.containsAll(Arrays.asList(e1, e2)));
        assertFalse(set.containsAll(Arrays.asList(e1, new GlobalEdge(n1, n2)))); // New edge not in set
        assertThrows(NullPointerException.class, () -> set.containsAll(null));
    }

    @Test
    public void testRetainAll() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2, e3);
        assertTrue(set.retainAll(Arrays.asList(e1, e2)));
        assertEquals(2, set.size());
        assertTrue(set.contains(e1));
        assertTrue(set.contains(e2));
        assertFalse(set.contains(e3));

        assertFalse(set.retainAll(Arrays.asList(e1, e2))); // No change
        assertThrows(NullPointerException.class, () -> set.retainAll(null));
    }

    @Test
    public void testRemoveAll() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2, e3);
        assertTrue(set.removeAll(Arrays.asList(e1, e2)));
        assertEquals(1, set.size());
        assertTrue(set.contains(e3));

        assertFalse(set.removeAll(Arrays.asList(e1, e2))); // No change
        assertThrows(NullPointerException.class, () -> set.removeAll(null));
    }

    @Test
    public void testTaggedWithAny() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2, e3);

        EdgeSet test1Set = set.taggedWithAny("test1");
        assertEquals(1, test1Set.size());
        assertTrue(test1Set.contains(e1));

        EdgeSet commonSet = set.taggedWithAny("common");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(e1));
        assertTrue(commonSet.contains(e2));

        EdgeSet multiTagSet = set.taggedWithAny("test1", "test3");
        assertEquals(2, multiTagSet.size());
        assertTrue(multiTagSet.contains(e1));
        assertTrue(multiTagSet.contains(e3));

        EdgeSet missingSet = set.taggedWithAny("missing");
        assertTrue(missingSet.isEmpty());

        EdgeSet nullTagsSet = set.taggedWithAny((String[]) null);
        assertTrue(nullTagsSet.isEmpty());

        EdgeSet emptyTagsSet = set.taggedWithAny();
        assertTrue(emptyTagsSet.isEmpty());
    }

    @Test
    public void testTaggedWithAll() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2, e3);

        EdgeSet test1Set = set.taggedWithAll("test1");
        assertEquals(1, test1Set.size());
        assertTrue(test1Set.contains(e1));

        EdgeSet commonSet = set.taggedWithAll("common");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(e1));
        assertTrue(commonSet.contains(e2));

        EdgeSet bothSet = set.taggedWithAll("test1", "common");
        assertEquals(1, bothSet.size());
        assertTrue(bothSet.contains(e1));

        EdgeSet missingSet = set.taggedWithAll("test1", "missing");
        assertTrue(missingSet.isEmpty());

        EdgeSet nullTagsSet = set.taggedWithAll((String[]) null);
        assertTrue(nullTagsSet.isEmpty());

        EdgeSet emptyTagsSet = set.taggedWithAll();
        assertTrue(emptyTagsSet.isEmpty());
    }

    @Test
    public void testIteratorAndToArray() {
        GlobalEdgeSet set = new GlobalEdgeSet(e1, e2);

        int count = 0;
        Iterator<Edge> it = set.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            assertTrue(e == e1 || e == e2);
            count++;
        }
        assertEquals(2, count);

        Object[] objArray = set.toArray();
        assertEquals(2, objArray.length);

        Edge[] edgeArray = set.toArray(new Edge[0]);
        assertEquals(2, edgeArray.length);
    }

    @Test
    public void testToStringAndEqualsAndHashCode() {
        GlobalEdgeSet set1 = new GlobalEdgeSet(e1, e2);
        GlobalEdgeSet set2 = new GlobalEdgeSet(e2, e1);
        GlobalEdgeSet set3 = new GlobalEdgeSet(e1);

        assertTrue(set1.toString().contains(e1.toString()));
        assertTrue(set1.toString().contains(e2.toString()));
        assertTrue(set1.toString().startsWith("GlobalEdgeSet [edges="));

        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());
        assertNotEquals(set1, set3);
        assertNotEquals(set1, "Not a set");
    }
}
