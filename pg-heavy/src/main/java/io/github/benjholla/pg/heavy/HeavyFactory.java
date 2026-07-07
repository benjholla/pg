package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.EdgeFactory;
import io.github.benjholla.pg.api.GraphFactory;
import io.github.benjholla.pg.api.NodeFactory;

/**
 * A factory interface for creating heavyweight graph components.
 */
public interface HeavyFactory extends NodeFactory, EdgeFactory, GraphFactory {

}
