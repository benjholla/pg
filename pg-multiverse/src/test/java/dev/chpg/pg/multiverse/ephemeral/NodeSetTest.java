package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class NodeSetTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    private Node n1, n2, n3;
    private NodeSet nodeSet;

    @BeforeEach
    public void setUp() {
        n1 = factory.createNode();
        n1.attributes().put("type", "A");
        n1.attributes().put("val", 1);

        n2 = factory.createNode();
        n2.attributes().put("type", "B");
        n2.attributes().put("val", 2);

        n3 = factory.createNode();
        n3.attributes().put("type", "A");
        n3.attributes().put("val", 3);

        nodeSet = new EphemeralNodeSet(n1, n2, n3);
    }

    @Test
    public void testConstructors() {
        NodeSet empty = new EphemeralNodeSet();
        assertTrue(empty.isEmpty());

        NodeSet single = new EphemeralNodeSet(n1);
        assertEquals(1, single.size());
        assertTrue(single.contains(n1));

        NodeSet multi = new EphemeralNodeSet(n1, n2);
        assertEquals(2, multi.size());
        assertTrue(multi.contains(n1));
        assertTrue(multi.contains(n2));

        NodeSet coll = new EphemeralNodeSet(Arrays.asList(n2, n3));
        assertEquals(2, coll.size());
        assertTrue(coll.contains(n2));
        assertTrue(coll.contains(n3));
    }

    @Test
    public void testOne() {
        Optional<Node> optNode = nodeSet.one();
        assertTrue(optNode.isPresent());
        assertTrue(nodeSet.contains(optNode.get()));

        NodeSet empty = new EphemeralNodeSet();
        assertFalse(empty.one().isPresent());
    }

    @Test
    public void testFilterByAttributePresent() {
        n1.attributes().put("unique", true);
        NodeSet filtered = nodeSet.attributedWith("unique");
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(n1));

        NodeSet filteredType = nodeSet.attributedWith("type");
        assertEquals(3, filteredType.size());
    }

    @Test
    public void testFilterByAttributeAndValues() {
        NodeSet filteredTypeA = nodeSet.attributedWith("type", AttributeValue.value("A"));
        assertEquals(2, filteredTypeA.size());
        assertTrue(filteredTypeA.contains(n1));
        assertTrue(filteredTypeA.contains(n3));

        NodeSet filteredVal = nodeSet.attributedWith("val", AttributeValue.value(1), AttributeValue.value(3));
        assertEquals(2, filteredVal.size());
        assertTrue(filteredVal.contains(n1));
        assertTrue(filteredVal.contains(n3));

        NodeSet filteredNone = nodeSet.attributedWith("type", AttributeValue.value("C"));
        assertTrue(filteredNone.isEmpty());

        NodeSet nullAttr = nodeSet.attributedWith(null, AttributeValue.value("A"));
        assertTrue(nullAttr.isEmpty());

        NodeSet nullVals = nodeSet.attributedWith("type", (AttributeValue[]) null);
        assertTrue(nullVals.isEmpty());

        NodeSet nullAttrOnly = nodeSet.attributedWith((String) null);
        assertTrue(nullAttrOnly.isEmpty());

        NodeSet nullAttrAndNullVals = nodeSet.attributedWith(null, (AttributeValue[]) null);
        assertTrue(nullAttrAndNullVals.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        NodeSet ns1 = new EphemeralNodeSet(n1, n2);
        NodeSet ns2 = new EphemeralNodeSet(n2, n1);
        assertEquals(ns1, ns2);
        assertEquals(ns1.hashCode(), ns2.hashCode());

        NodeSet ns3 = new EphemeralNodeSet(n1, n3);
        assertNotEquals(ns1, ns3);
    }

    @Test
    public void testToString() {
        NodeSet ns = new EphemeralNodeSet(n1);
        String str = ns.toString();
        assertTrue(str.startsWith("EphemeralNodeSet [nodes="));
        assertTrue(str.contains(n1.toString()));
        assertTrue(str.endsWith("]"));
    }
}
