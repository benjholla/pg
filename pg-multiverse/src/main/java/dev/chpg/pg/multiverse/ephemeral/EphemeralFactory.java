package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.EdgeFactory;
import dev.chpg.pg.api.GraphFactory;
import dev.chpg.pg.api.NodeFactory;

/**
 * A factory interface for creating ephemeral graph components.
 */
public interface EphemeralFactory extends NodeFactory, EdgeFactory, GraphFactory {

}
