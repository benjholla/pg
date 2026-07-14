package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;

public class EphemeralEdgeSetCoverageTest {

    private EphemeralFactory factory;
    private EphemeralEdge e1, e2, e3;
    private EphemeralNode n1, n2;

    @BeforeEach
    public void setUp() {
        factory = new EphemeralGraph().factory();
        n1 = (EphemeralNode) factory.createNode();
        n2 = (EphemeralNode) factory.createNode();

        e1 = (EphemeralEdge) factory.createEdge(n1, n2);
        e1.tags().add("test1");
        e1.tags().add("common");
        e1.attributes().put("attr1", "val1");
        e1.attributes().put("common_attr", "common_val");

        e2 = (EphemeralEdge) factory.createEdge(n2, n1);
        e2.tags().add("test2");
        e2.tags().add("common");
        e2.attributes().put("attr2", "val2");
        e2.attributes().put("common_attr", "common_val");

        e3 = (EphemeralEdge) factory.createEdge(n1, n1);
        e3.tags().add("test3");
    }

    @Test
    public void testConstructors() {
        EphemeralEdgeSet empty = new EphemeralEdgeSet();
        assertTrue(empty.isEmpty());

        EphemeralEdgeSet single = new EphemeralEdgeSet(e1);
        assertEquals(1, single.size());

        EphemeralEdgeSet array = new EphemeralEdgeSet(e1, e2);
        assertEquals(2, array.size());
        assertThrows(NullPointerException.class, () -> new EphemeralEdgeSet((Edge[]) null));

        EphemeralEdgeSet collection = new EphemeralEdgeSet(Arrays.asList(e1, e2, e3));
        assertEquals(3, collection.size());
        assertThrows(NullPointerException.class, () -> new EphemeralEdgeSet((Collection<Edge>) null));
    }

    @Test
    public void testValidate() {
        EphemeralEdgeSet set = new EphemeralEdgeSet();
        assertThrows(NullPointerException.class, () -> set.add(null));

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
        EphemeralEdgeSet emptySet = new EphemeralEdgeSet();
        EdgeSet emptyImmutable = emptySet.toImmutable();
        assertTrue(emptyImmutable.isEmpty());
        assertTrue(emptyImmutable instanceof dev.chpg.pg.api.EdgeSet);

        EphemeralEdgeSet singletonSet = new EphemeralEdgeSet(e1);
        EdgeSet singletonImmutable = singletonSet.toImmutable();
        assertEquals(1, singletonImmutable.size());
        assertTrue(singletonImmutable instanceof EphemeralImmutableSingletonEdgeSet);

        EphemeralEdgeSet multiSet = new EphemeralEdgeSet(e1, e2);
        EdgeSet multiImmutable = multiSet.toImmutable();
        assertEquals(2, multiImmutable.size());
        assertTrue(multiImmutable instanceof EphemeralImmutableEdgeSet);
    }

    @Test
    public void testOne() {
        EphemeralEdgeSet emptySet = new EphemeralEdgeSet();
        assertFalse(emptySet.one().isPresent());

        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2);
        Optional<Edge> one = set.one();
        assertTrue(one.isPresent());
        assertTrue(one.get() == e1 || one.get() == e2);
    }

    @Test
    public void testwithAttribute() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2, e3);

        EdgeSet attr1Set = set.withAttribute("attr1");
        assertEquals(1, attr1Set.size());
        assertTrue(attr1Set.contains(e1));

        EdgeSet commonSet = set.withAttribute("common_attr");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(e1));
        assertTrue(commonSet.contains(e2));

        EdgeSet missingSet = set.withAttribute("missing_attr");
        assertTrue(missingSet.isEmpty());
    }

    @Test
    public void testFilterWithValues() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2, e3);

        EdgeSet val1Set = set.withAttribute("attr1", AttributeValue.value("val1"));
        assertEquals(1, val1Set.size());
        assertTrue(val1Set.contains(e1));

        EdgeSet val2Set = set.withAttribute("attr2", AttributeValue.value("val2"), AttributeValue.value("other"));
        assertEquals(1, val2Set.size());
        assertTrue(val2Set.contains(e2));

        EdgeSet missingSet = set.withAttribute("attr1", AttributeValue.value("missing"));
        assertTrue(missingSet.isEmpty());

        EdgeSet nullAttrSet = set.withAttribute(null, AttributeValue.value("val1"));
        assertTrue(nullAttrSet.isEmpty());

        EdgeSet nullValsSet = set.withAttribute("attr1", (AttributeValue[]) null);
        assertTrue(nullValsSet.isEmpty());

        EdgeSet withNullValSet = set.withAttribute("attr1", new AttributeValue[]{null});
        assertTrue(withNullValSet.isEmpty());
    }

    @Test
    public void testIntersect() {
        EphemeralEdgeSet set1 = new EphemeralEdgeSet(e1, e2);
        EphemeralEdgeSet set2 = new EphemeralEdgeSet(e2, e3);

        EdgeSet intersectSet = set1.intersect(set2);
        assertEquals(1, intersectSet.size());
        assertTrue(intersectSet.contains(e2));

        EdgeSet emptyIntersect = set1.intersect(new EphemeralEdgeSet(e3));
        assertTrue(emptyIntersect.isEmpty());

        EdgeSet nullIntersect = set1.intersect(null);
        assertTrue(nullIntersect.isEmpty());
    }

    @Test
    public void testDifference() {
        EphemeralEdgeSet set1 = new EphemeralEdgeSet(e1, e2);
        EphemeralEdgeSet set2 = new EphemeralEdgeSet(e2, e3);

        EdgeSet diffSet = set1.difference(set2);
        assertEquals(1, diffSet.size());
        assertTrue(diffSet.contains(e1));

        EdgeSet allDiff = set1.difference(new EphemeralEdgeSet(e3));
        assertEquals(2, allDiff.size());
        assertTrue(allDiff.contains(e1));
        assertTrue(allDiff.contains(e2));

        EdgeSet nullDiff = set1.difference(null);
        assertEquals(2, nullDiff.size());
    }

    @Test
    public void testUnion() {
        EphemeralEdgeSet set1 = new EphemeralEdgeSet(e1);
        EphemeralEdgeSet set2 = new EphemeralEdgeSet(e2, e3);

        EdgeSet unionSet = set1.union(set2);
        assertEquals(3, unionSet.size());
        assertTrue(unionSet.contains(e1));
        assertTrue(unionSet.contains(e2));
        assertTrue(unionSet.contains(e3));

        EdgeSet nullUnion = set1.union(null);
        assertEquals(1, nullUnion.size());
        assertTrue(nullUnion.contains(e1));

        class InvalidEdge implements Edge {
            @Override public int id() { return 0; }
            @Override public dev.chpg.pg.api.TagSet tags() { return null; }
            @Override public dev.chpg.pg.api.AttributeMap attributes() { return null; }
            @Override public Node from() { return null; }
            @Override public Node to() { return null; }
        }
        EdgeSet mixUnion = set1.union(Arrays.asList(e2, new InvalidEdge()));
        assertEquals(2, mixUnion.size());
        assertTrue(mixUnion.contains(e1));
        assertTrue(mixUnion.contains(e2));
    }

    @Test
    public void testIds() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2);
        Set<Integer> ids = set.ids();
        assertEquals(2, ids.size());
        assertTrue(ids.contains(e1.id()));
        assertTrue(ids.contains(e2.id()));
    }

    @Test
    public void testToIdArray() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2);
        int[] ids = set.toIdArray();
        assertEquals(2, ids.length);
        assertTrue(ids[0] == e1.id() || ids[0] == e2.id());
        assertTrue(ids[1] == e1.id() || ids[1] == e2.id());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testContains() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1);
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

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testRemove() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2);
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
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2);
        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    public void testContainsAll() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2, e3);
        assertTrue(set.containsAll(Arrays.asList(e1, e2)));
        assertFalse(set.containsAll(Arrays.asList(e1, factory.createEdge(n1, n2))));
        assertThrows(NullPointerException.class, () -> set.containsAll(null));
    }

    @Test
    public void testRetainAll() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2, e3);
        assertTrue(set.retainAll(Arrays.asList(e1, e2)));
        assertEquals(2, set.size());
        assertTrue(set.contains(e1));
        assertTrue(set.contains(e2));
        assertFalse(set.contains(e3));

        assertFalse(set.retainAll(Arrays.asList(e1, e2)));
        assertThrows(NullPointerException.class, () -> set.retainAll(null));
    }

    @Test
    public void testRemoveAll() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2, e3);
        assertTrue(set.removeAll(Arrays.asList(e1, e2)));
        assertEquals(1, set.size());
        assertTrue(set.contains(e3));

        assertFalse(set.removeAll(Arrays.asList(e1, e2)));
        assertThrows(NullPointerException.class, () -> set.removeAll(null));
    }

    @Test
    public void testwithAnyTag() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2, e3);

        EdgeSet test1Set = set.withAnyTag("test1");
        assertEquals(1, test1Set.size());
        assertTrue(test1Set.contains(e1));

        EdgeSet commonSet = set.withAnyTag("common");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(e1));
        assertTrue(commonSet.contains(e2));

        EdgeSet multiTagSet = set.withAnyTag("test1", "test3");
        assertEquals(2, multiTagSet.size());
        assertTrue(multiTagSet.contains(e1));
        assertTrue(multiTagSet.contains(e3));

        EdgeSet missingSet = set.withAnyTag("missing");
        assertTrue(missingSet.isEmpty());

        EdgeSet nullTagsSet = set.withAnyTag((String[]) null);
        assertTrue(nullTagsSet.isEmpty());

        EdgeSet emptyTagsSet = set.withAnyTag();
        assertTrue(emptyTagsSet.isEmpty());
    }

    @Test
    public void testwithAllTags() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2, e3);

        EdgeSet test1Set = set.withAllTags("test1");
        assertEquals(1, test1Set.size());
        assertTrue(test1Set.contains(e1));

        EdgeSet commonSet = set.withAllTags("common");
        assertEquals(2, commonSet.size());
        assertTrue(commonSet.contains(e1));
        assertTrue(commonSet.contains(e2));

        EdgeSet bothSet = set.withAllTags("test1", "common");
        assertEquals(1, bothSet.size());
        assertTrue(bothSet.contains(e1));

        EdgeSet missingSet = set.withAllTags("test1", "missing");
        assertTrue(missingSet.isEmpty());

        EdgeSet nullTagsSet = set.withAllTags((String[]) null);
        assertTrue(nullTagsSet.isEmpty());

        EdgeSet emptyTagsSet = set.withAllTags();
        assertTrue(emptyTagsSet.isEmpty());
    }

    @Test
    public void testIteratorAndToArray() {
        EphemeralEdgeSet set = new EphemeralEdgeSet(e1, e2);

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
        EphemeralEdgeSet set1 = new EphemeralEdgeSet(e1, e2);
        EphemeralEdgeSet set2 = new EphemeralEdgeSet(e2, e1);
        EphemeralEdgeSet set3 = new EphemeralEdgeSet(e1);

        assertTrue(set1.toString().contains(e1.toString()));
        assertTrue(set1.toString().contains(e2.toString()));
        assertTrue(set1.toString().startsWith("EphemeralEdgeSet [edges="));

        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());
        assertNotEquals(set1, set3);
        assertNotEquals(set1, "Not a set");
    }
}
