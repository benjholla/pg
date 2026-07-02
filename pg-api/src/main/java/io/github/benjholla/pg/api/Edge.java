package io.github.benjholla.pg.api;

public interface Edge extends GraphElement {
    Node from();
    Node to();
}
