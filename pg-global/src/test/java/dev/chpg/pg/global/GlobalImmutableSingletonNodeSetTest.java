package dev.chpg.pg.global;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
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

public class GlobalImmutableSingletonNodeSetTest {

    private GlobalGraph graph;
    private GlobalNode node;
    private GlobalImmutableSingletonNodeSet singletonSet;

    @BeforeEach
    public void setup() {
        graph = new GlobalGraph();
        node = graph.createNode();
        singletonSet = new GlobalImmutableSingletonNodeSet(node);
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
        assertTrue(singletonSet.contains(node));

        GlobalNode otherNode = graph.createNode();
        assertFalse(singletonSet.contains(otherNode));
        assertFalse(singletonSet.contains(new Object()));
        assertFalse(singletonSet.contains(null));
    }

    @Test
    public void testIterator() {
        Iterator<Node> iterator = singletonSet.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(node, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testOne() {
        Optional<Node> one = singletonSet.one();
        assertTrue(one.isPresent());
        assertEquals(node, one.get());
    }

    @Test
    public void testFilterByAttribute() {
        node.attributes().put("key", AttributeValue.value("value"));

        NodeSet filtered = singletonSet.withAttribute("key");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(node));

        NodeSet empty = singletonSet.withAttribute("otherKey");
        assertEquals(0, empty.size());
    }

    @Test
    public void testFilterByAttributeAndValue() {
        node.attributes().put("key", AttributeValue.value("value"));

        NodeSet filtered = singletonSet.withAttribute("key", AttributeValue.value("value"));
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(node));

        NodeSet filteredMulti = singletonSet.withAttribute("key", AttributeValue.value("other"), AttributeValue.value("value"));
        assertEquals(1, filteredMulti.size());
        assertTrue(filteredMulti.contains(node));

        NodeSet empty = singletonSet.withAttribute("key", AttributeValue.value("other"));
        assertEquals(0, empty.size());

        NodeSet emptyMissing = singletonSet.withAttribute("missing", AttributeValue.value("value"));
        assertEquals(0, emptyMissing.size());
    }

    @Test
    public void testIntersect() {
        NodeSet intersectSelf = singletonSet.intersect(Collections.singleton(node));
        assertEquals(1, intersectSelf.size());
        assertTrue(intersectSelf.contains(node));

        GlobalNode otherNode = graph.createNode();
        NodeSet intersectEmpty = singletonSet.intersect(Collections.singleton(otherNode));
        assertEquals(0, intersectEmpty.size());
    }

    @Test
    public void testDifference() {
        NodeSet diffSelf = singletonSet.difference(Collections.singleton(node));
        assertEquals(0, diffSelf.size());

        GlobalNode otherNode = graph.createNode();
        NodeSet diffEmpty = singletonSet.difference(Collections.singleton(otherNode));
        assertEquals(1, diffEmpty.size());
        assertTrue(diffEmpty.contains(node));
    }

    @Test
    public void testUnion() {
        NodeSet unionSelf = singletonSet.union(Collections.singleton(node));
        assertEquals(1, unionSelf.size());
        assertTrue(unionSelf.contains(node));

        GlobalNode otherNode = graph.createNode();
        NodeSet unionOther = singletonSet.union(Arrays.asList(node, otherNode));
        assertEquals(2, unionOther.size());
        assertTrue(unionOther.contains(node));
        assertTrue(unionOther.contains(otherNode));

        NodeSet unionDisjoint = singletonSet.union(Collections.singleton(otherNode));
        assertEquals(2, unionDisjoint.size());
        assertTrue(unionDisjoint.contains(node));
        assertTrue(unionDisjoint.contains(otherNode));
    }

    @Test
    public void testIds() {
        Set<Integer> ids = singletonSet.ids();
        assertEquals(1, ids.size());
        assertTrue(ids.contains(node.id()));
    }

    @Test
    public void testToIdArray() {
        int[] idArray = singletonSet.toIdArray();
        assertArrayEquals(new int[]{node.id()}, idArray);
    }
}
