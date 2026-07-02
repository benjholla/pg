package io.github.benjholla.pg.api;

import java.util.Optional;
import java.util.Set;

public interface NodeSet extends Set<Node> {
    Optional<Node> one();
    NodeSet filter(String attribute);
    NodeSet filter(String attribute, Object... values);
}
