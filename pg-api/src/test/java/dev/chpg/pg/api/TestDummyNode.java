package dev.chpg.pg.api;

public class TestDummyNode implements Node {
    @Override
    public int id() { return 1; }

    @Override
    public TagSet tags() { return null; }

    @Override
    public AttributeMap attributes() { return null; }
}
