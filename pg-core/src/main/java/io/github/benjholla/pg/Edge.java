package io.github.benjholla.pg;

public interface Edge extends GraphElement {
    Node from();
    Node to();
}
