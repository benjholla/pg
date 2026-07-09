package io.github.benjholla.pg.global;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GlobalImmutableSingletonEdgeSetTest {

    private GlobalGraph graph;
    private GlobalEdge edge;
    private GlobalImmutableSingletonEdgeSet singletonSet;

    @BeforeEach
    public void setup() {
        graph = new GlobalGraph();
        GlobalNode n1 = graph.createNode();
        GlobalNode n2 = graph.createNode();
        edge = graph.createEdge(n1, n2);
        singletonSet = new GlobalImmutableSingletonEdgeSet(edge);
    }

    @Test
    public void testToImmutable() {
        assertEquals(singletonSet, singletonSet.toImmutable());
    }

    @Test
    public void testSize() {
        assertEquals(1, singletonSet.size());
    }

    @Test
    public void testContains() {
        assertTrue(singletonSet.contains(edge));

        GlobalNode n3 = graph.createNode();
        GlobalEdge otherEdge = graph.createEdge(n3, n3);
        assertFalse(singletonSet.contains(otherEdge));
        assertFalse(singletonSet.contains(new Object()));
        assertFalse(singletonSet.contains(null));
    }

    @Test
    public void testIterator() {
        Iterator<Edge> iterator = singletonSet.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(edge, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testOne() {
        Optional<Edge> one = singletonSet.one();
        assertTrue(one.isPresent());
        assertEquals(edge, one.get());
    }

    @Test
    public void testFilterByAttribute() {
        edge.attributes().put("key", new AttributeValue.StringVal("value"));

        EdgeSet filtered = singletonSet.filter("key");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(edge));

        EdgeSet empty = singletonSet.filter("otherKey");
        assertEquals(0, empty.size());
    }

    @Test
    public void testFilterByAttributeAndValue() {
        edge.attributes().put("key", new AttributeValue.StringVal("value"));

        EdgeSet filtered = singletonSet.filter("key", new AttributeValue.StringVal("value"));
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(edge));

        EdgeSet filteredMulti = singletonSet.filter("key", new AttributeValue.StringVal("other"), new AttributeValue.StringVal("value"));
        assertEquals(1, filteredMulti.size());
        assertTrue(filteredMulti.contains(edge));

        EdgeSet empty = singletonSet.filter("key", new AttributeValue.StringVal("other"));
        assertEquals(0, empty.size());

        EdgeSet emptyMissing = singletonSet.filter("missing", new AttributeValue.StringVal("value"));
        assertEquals(0, emptyMissing.size());
    }

    @Test
    public void testIntersect() {
        EdgeSet intersectSelf = singletonSet.intersect(Collections.singleton(edge));
        assertEquals(1, intersectSelf.size());
        assertTrue(intersectSelf.contains(edge));

        GlobalNode n3 = graph.createNode();
        GlobalEdge otherEdge = graph.createEdge(n3, n3);
        EdgeSet intersectEmpty = singletonSet.intersect(Collections.singleton(otherEdge));
        assertEquals(0, intersectEmpty.size());
    }

    @Test
    public void testDifference() {
        EdgeSet diffSelf = singletonSet.difference(Collections.singleton(edge));
        assertEquals(0, diffSelf.size());

        GlobalNode n3 = graph.createNode();
        GlobalEdge otherEdge = graph.createEdge(n3, n3);
        EdgeSet diffEmpty = singletonSet.difference(Collections.singleton(otherEdge));
        assertEquals(1, diffEmpty.size());
        assertTrue(diffEmpty.contains(edge));
    }

    @Test
    public void testUnion() {
        EdgeSet unionSelf = singletonSet.union(Collections.singleton(edge));
        assertEquals(1, unionSelf.size());
        assertTrue(unionSelf.contains(edge));

        GlobalNode n3 = graph.createNode();
        GlobalEdge otherEdge = graph.createEdge(n3, n3);
        EdgeSet unionOther = singletonSet.union(Arrays.asList(edge, otherEdge));
        assertEquals(2, unionOther.size());
        assertTrue(unionOther.contains(edge));
        assertTrue(unionOther.contains(otherEdge));

        EdgeSet unionDisjoint = singletonSet.union(Collections.singleton(otherEdge));
        assertEquals(2, unionDisjoint.size());
        assertTrue(unionDisjoint.contains(edge));
        assertTrue(unionDisjoint.contains(otherEdge));
    }

    @Test
    public void testIds() {
        Set<Integer> ids = singletonSet.ids();
        assertEquals(1, ids.size());
        assertTrue(ids.contains(edge.id()));
    }

    @Test
    public void testToIdArray() {
        int[] idArray = singletonSet.toIdArray();
        assertArrayEquals(new int[]{edge.id()}, idArray);
    }
}
