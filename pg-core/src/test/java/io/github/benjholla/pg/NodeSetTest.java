package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Optional;

public class NodeSetTest {

    private Node n1, n2, n3;
    private NodeSet nodeSet;

    @BeforeEach
    public void setUp() {
        n1 = new Node();
        n1.putAttr("type", "A");
        n1.putAttr("val", 1);

        n2 = new Node();
        n2.putAttr("type", "B");
        n2.putAttr("val", 2);

        n3 = new Node();
        n3.putAttr("type", "A");
        n3.putAttr("val", 3);

        nodeSet = new NodeSet(n1, n2, n3);
    }

    @Test
    public void testConstructors() {
        NodeSet empty = new NodeSet();
        assertTrue(empty.isEmpty());

        NodeSet single = new NodeSet(n1);
        assertEquals(1, single.size());
        assertTrue(single.contains(n1));

        NodeSet multi = new NodeSet(n1, n2);
        assertEquals(2, multi.size());
        assertTrue(multi.contains(n1));
        assertTrue(multi.contains(n2));

        NodeSet coll = new NodeSet(Arrays.asList(n2, n3));
        assertEquals(2, coll.size());
        assertTrue(coll.contains(n2));
        assertTrue(coll.contains(n3));
    }

    @Test
    public void testOne() {
        Optional<Node> optNode = nodeSet.one();
        assertTrue(optNode.isPresent());
        assertTrue(nodeSet.contains(optNode.get()));

        NodeSet empty = new NodeSet();
        assertFalse(empty.one().isPresent());
    }

    @Test
    public void testFilterByAttributePresent() {
        n1.putAttr("unique", true);
        NodeSet filtered = nodeSet.filter("unique");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(n1));

        NodeSet filteredType = nodeSet.filter("type");
        assertEquals(3, filteredType.size());
    }

    @Test
    public void testFilterByAttributeAndValues() {
        NodeSet filteredTypeA = nodeSet.filter("type", "A");
        assertEquals(2, filteredTypeA.size());
        assertTrue(filteredTypeA.contains(n1));
        assertTrue(filteredTypeA.contains(n3));

        NodeSet filteredVal = nodeSet.filter("val", 1, 3);
        assertEquals(2, filteredVal.size());
        assertTrue(filteredVal.contains(n1));
        assertTrue(filteredVal.contains(n3));

        NodeSet filteredNone = nodeSet.filter("type", "C");
        assertTrue(filteredNone.isEmpty());

        NodeSet nullAttr = nodeSet.filter(null, "A");
        assertTrue(nullAttr.isEmpty());

        NodeSet nullVals = nodeSet.filter("type", (Object[]) null);
        assertTrue(nullVals.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        NodeSet ns1 = new NodeSet(n1, n2);
        NodeSet ns2 = new NodeSet(n2, n1);
        assertEquals(ns1, ns2);
        assertEquals(ns1.hashCode(), ns2.hashCode());

        NodeSet ns3 = new NodeSet(n1, n3);
        assertNotEquals(ns1, ns3);
    }

    @Test
    public void testToString() {
        NodeSet ns = new NodeSet(n1);
        String str = ns.toString();
        assertTrue(str.startsWith("NodeSet [nodes="));
        assertTrue(str.contains(n1.toString()));
        assertTrue(str.endsWith("]"));
    }
}
