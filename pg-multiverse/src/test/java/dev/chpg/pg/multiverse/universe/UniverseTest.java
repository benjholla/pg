package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralEdge;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNode;
import org.junit.jupiter.api.Test;

public class UniverseTest {

    @Test
    public void testDefaultConstructor() {
        Universe universe = new Universe();
        assertNotNull(universe.idGenerator(), "ID generator should not be null");
        assertEquals(0L, universe.modCount(), "Initial modCount should be 0");
    }

    @Test
    public void testInjectedConstructor() {
        UniverseIdGenerator generator = new UniverseIdGenerator();
        generator.incrementAndGetModCount(); // Make it non-zero for testing

        Universe universe = new Universe(generator);
        assertSame(generator, universe.idGenerator(), "Should return injected ID generator");
        assertEquals(1L, universe.modCount(), "Should reflect injected generator's modCount");
    }

    @Test
    public void testUniverseIdUniqueness() {
        Universe u1 = new Universe();
        Universe u2 = new Universe();
        Universe u3 = new Universe(new UniverseIdGenerator());


        assertNotEquals(u1.universeId(), u2.universeId(), "Universe IDs must be unique");
        assertNotEquals(u1.universeId(), u3.universeId(), "Universe IDs must be unique");
        assertNotEquals(u2.universeId(), u3.universeId(), "Universe IDs must be unique");

        assertEquals(u2.universeId(), u1.universeId() + 1, "Universe IDs should be monotonically increasing");
        assertEquals(u3.universeId(), u2.universeId() + 1, "Universe IDs should be monotonically increasing");
    }

    @Test
    public void testNullInjectedConstructor() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new Universe(null);
        });
        assertEquals("IdGenerator cannot be null", exception.getMessage());
    }

    @Test
    public void testIncrementModCount() {
        Universe universe = new Universe();
        assertEquals(0L, universe.modCount());

        long newCount = universe.incrementModCount();
        assertEquals(1L, newCount);
        assertEquals(1L, universe.modCount());

        universe.incrementModCount();
        assertEquals(2L, universe.modCount());
    }

    @Test
    public void testPromote() {
        Universe universe = new Universe();
        EphemeralGraph eg = new EphemeralGraph(universe);

        EphemeralNode n1 = eg.createNode();
        n1.tags().add("User");
        n1.attributes().put("name", AttributeValue.value("Alice"));

        EphemeralNode n2 = eg.createNode();
        n2.tags().add("User");
        n2.attributes().put("name", AttributeValue.value("Bob"));

        EphemeralEdge e1 = eg.createEdge(n1, n2);
        e1.tags().add("KNOWS");
        e1.attributes().put("since", AttributeValue.value("2023"));

        eg.addNode(n1);
        eg.addNode(n2);
        eg.addEdge(e1);

        assertEquals(2, eg.nodes().size());
        assertEquals(1, eg.edges().size());

        UniverseGraph ug = universe.promote(eg);

        // Assert ephemeral is empty
        assertEquals(0, eg.nodes().size());
        assertEquals(0, eg.edges().size());

        // Assert universe graph properties
        assertEquals(2, ug.nodes().size());
        assertEquals(1, ug.edges().size());

        // Nodes exist and have correct tags and properties
        Node un1 = null;
        Node un2 = null;
        for (Node un : ug.nodes()) {
            assertTrue(un.tags().contains("User"));
            if (((AttributeValue.StringValue) un.attributes().get("name")).value().equals("Alice")) {
                un1 = un;
            } else if (((AttributeValue.StringValue) un.attributes().get("name")).value().equals("Bob")) {
                un2 = un;
            }
        }

        assertNotNull(un1);
        assertNotNull(un2);

        Edge ue1 = null;
        for (Edge ue : ug.edges()) {
            ue1 = ue;
        }

        assertNotNull(ue1);
        assertTrue(ue1.tags().contains("KNOWS"));
        assertEquals("2023", ((AttributeValue.StringValue) ue1.attributes().get("since")).value());

        // Assert structure
        assertTrue(ue1.from().equals(un1) || ue1.from().equals(un2));
        assertTrue(ue1.to().equals(un1) || ue1.to().equals(un2));

        if (ue1.from().equals(un1)) {
            assertEquals(un2, ue1.to());
        } else {
            assertEquals(un1, ue1.to());
        }
    }

    @Test
    public void testPromoteNull() {
        Universe universe = new Universe();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            universe.promote(null);
        });
        assertEquals("EphemeralGraph cannot be null", exception.getMessage());
    }

    @Test
    public void testToString() {
        Universe universe = new Universe();
        assertEquals("Universe[id=" + universe.universeId() + ", allocatedNodes=1, allocatedEdges=1]", universe.toString());

        universe.idGenerator().createNodeId();
        universe.idGenerator().createNodeId();
        universe.idGenerator().createEdgeId();

        assertEquals("Universe[id=" + universe.universeId() + ", allocatedNodes=3, allocatedEdges=2]", universe.toString());
    }
}
