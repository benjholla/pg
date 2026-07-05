package io.github.benjholla.pg.heavy;

import java.util.Comparator;

import io.github.benjholla.pg.api.Graph;

public class HeavyGraphSort {

    public static final Comparator<Graph> GRAPH_SIZE_COMPARATOR = new Comparator<Graph>() {
        @Override
        public int compare(Graph g1, Graph g2) {
            int nodes = Integer.compare(g1.nodes().size(), g2.nodes().size());
            if(nodes != 0) {
                return nodes;
            } else {
                return Integer.compare(g1.edges().size(), g2.edges().size());
            }
        }
    };
    
}
