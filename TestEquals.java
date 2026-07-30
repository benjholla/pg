import dev.chpg.pg.multiverse.ephemeral.*;
import dev.chpg.pg.multiverse.universe.*;
import dev.chpg.pg.api.*;
import java.util.*;

public class TestEquals {
    public static void main(String[] args) {
        Universe u = new Universe();
        EphemeralGraph g = new EphemeralGraph(u);
        Node n1 = g.factory().createNode();
        Edge e1 = g.factory().createEdge(n1, n1);
        g.addEdge(e1); // Wraps it in ShadowEdge

        Set<Edge> s = new HashSet<>();
        for (Edge e : g.edges()) {
            s.add(e);
        }
        System.out.println("Contains? " + s.contains(e1));
        System.out.println("Set elements: " + s);
        System.out.println("e1: " + e1);
    }
}
