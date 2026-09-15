package dev.chpg.pg.api;

/**
 * A comprehensive factory interface that combines node, edge, and graph creation capabilities.
 * <p>
 * <b>What it represents:</b> A unified factory for creating graph elements ({@link Node}s, {@link Edge}s) and {@link Graph}s.
 * <p>
 * <b>Why it exists:</b> To provide a single, cohesive interface for instantiating property graph components, decoupling the consumer from specific backend implementations.
 * <p>
 * <b>When to use it:</b> Use {@code Factory} when you need to construct graphs and their underlying elements without depending on concrete classes.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Obtaining a factory from a specific engine (e.g., {@code Factory factory = new GlobalGraph().factory();}).</li>
 * <li>Creating nodes, edges, and assembling graphs programmatically.</li>
 * </ul>
 */
public interface Factory extends NodeFactory, EdgeFactory, GraphFactory {

}
