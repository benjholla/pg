package dev.chpg.pg.multiverse.universe;

/**
 * A capability interface indicating that the implementing element is
 * mathematically bound to a specific Universe storage engine.
 */
public interface UniverseView {

    /**
     * Exposes the underlying bitwise storage engine backing this element.
     *
     * @return the backing Universe
     */
    Universe universe();

}
