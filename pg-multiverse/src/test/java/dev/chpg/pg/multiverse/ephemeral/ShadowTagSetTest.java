package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

import java.util.Iterator;

public class ShadowTagSetTest {
    @Test
    public void testShadowTagSetNode() {
        Universe universe = new Universe();
        int nId = universe.idGenerator().createNodeId();
        UniverseNode n = new UniverseNode(universe, nId);
        universe.asGraph().addNode(n);

        EphemeralGraph graph = new EphemeralGraph(universe);
        ShadowNode sNode = new ShadowNode(graph, n);

        assertTrue(sNode.tags().add("tag1"));
        assertTrue(sNode.tags().contains("tag1"));
        assertTrue(sNode.tags().remove("tag1"));
        assertFalse(sNode.tags().contains("tag1"));
        assertFalse(sNode.tags().remove("tag1"));
        assertFalse(sNode.tags().contains(123)); // test object
        assertFalse(sNode.tags().remove(123)); // test object

        n.tags().add("universe_tag");
        assertTrue(sNode.tags().contains("universe_tag"));
        assertTrue(sNode.tags().remove("universe_tag"));
        assertFalse(sNode.tags().contains("universe_tag"));
        assertTrue(n.tags().contains("universe_tag")); // Should still be in universe until promoted

        // Add a pending tag and iterate
        sNode.tags().add("tag2");
        sNode.tags().add("tag3");

        int count = 0;
        for (String tag : sNode.tags()) {
            count++;
        }
        assertTrue(count == 2);
    }

    @Test
    public void testShadowTagSetEdge() {
        Universe universe = new Universe();

        EphemeralGraph graph = new EphemeralGraph(universe);

        EphemeralNode n1 = new EphemeralNode(universe, -1);
        graph.addNode(n1);
        EphemeralNode n2 = new EphemeralNode(universe, -2);
        graph.addNode(n2);

        EphemeralEdge e = new EphemeralEdge(universe, -1, n1, n2);
        graph.addEdge(e);

        universe.promote(graph);

        UniverseEdge createdEdge = (UniverseEdge) universe.asGraph().edges().iterator().next();

        EphemeralGraph graph2 = new EphemeralGraph(universe);
        ShadowEdge sEdge = new ShadowEdge(graph2, createdEdge);

        assertTrue(sEdge.tags().add("tag1"));
        assertTrue(sEdge.tags().contains("tag1"));
        assertTrue(sEdge.tags().remove("tag1"));
        assertFalse(sEdge.tags().contains("tag1"));
        assertFalse(sEdge.tags().remove("tag1"));
        assertFalse(sEdge.tags().contains(123)); // test object
        assertFalse(sEdge.tags().remove(123)); // test object

        createdEdge.tags().add("universe_tag");
        assertTrue(sEdge.tags().contains("universe_tag"));
        assertTrue(sEdge.tags().remove("universe_tag"));
        assertFalse(sEdge.tags().contains("universe_tag"));
        assertTrue(createdEdge.tags().contains("universe_tag")); // Should still be in universe until promoted
    }

    private Universe universeSetup;
    private EphemeralGraph graphSetup;
    private Node nSetup;
    private EphemeralFactory factory;

    @BeforeEach
    public void setup() {
        universeSetup = new Universe();
        graphSetup = new EphemeralGraph(universeSetup);
        factory = graphSetup.factory();

        nSetup = factory.createNode();
        graphSetup.addNode(nSetup);

        nSetup.tags().add("tag1");
        nSetup.tags().add("tag2");
    }

    @Test
    public void testShadowTagSetMethods() {
        setup();
        TagSet tags = nSetup.tags(); // returns ShadowTagSet internally when fetched

        assertEquals(2, tags.size());
        assertTrue(tags.contains("tag1"));

        Iterator<String> iter = tags.iterator();
        assertTrue(iter.hasNext());
        String t = iter.next();
        assertTrue(t.equals("tag1") || t.equals("tag2"));
    }
}
