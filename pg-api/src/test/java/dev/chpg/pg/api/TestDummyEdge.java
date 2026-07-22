package dev.chpg.pg.api;

public class TestDummyEdge implements Edge {
    @Override
    public int id() { return 1; }

    @Override
    public TagSet tags() { return null; }

    @Override
    public AttributeMap attributes() { return null; }

    @Override
    public Node from() { return new TestDummyNode(); }

    @Override
    public Node to() { return new TestDummyNode(); }
}
