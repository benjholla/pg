package dev.chpg.pg.global;

import dev.chpg.pg.api.EdgeFactory;
import dev.chpg.pg.api.GraphFactory;
import dev.chpg.pg.api.NodeFactory;

/**
 * A factory interface for creating globalweight graph components.
 */
public interface GlobalFactory extends NodeFactory, EdgeFactory, GraphFactory {

}
