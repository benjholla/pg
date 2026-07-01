package io.github.benjholla.pg;

import java.util.Optional;
import java.util.Set;

public interface EdgeSet extends Set<Edge> {
    Optional<Edge> one();
    EdgeSet filter(String attribute);
    EdgeSet filter(String attribute, Object... values);
}
