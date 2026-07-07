package io.github.benjholla.pg.universe.ephemeral;

import io.github.benjholla.pg.api.EdgeFactory;
import io.github.benjholla.pg.api.GraphFactory;
import io.github.benjholla.pg.api.NodeFactory;

/**
 * A factory interface for creating ephemeral graph components.
 */
public interface EphemeralFactory extends NodeFactory, EdgeFactory, GraphFactory {

}
