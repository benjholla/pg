package io.github.benjholla.pg;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Tags denote set membership
 */
public class TagSet extends HashSet<String> {

    private static final long serialVersionUID = 1L;

    public TagSet() {
        super();
    }

    public TagSet(Collection<? extends String> c) {
        super(c);
    }

    @Override
    public String toString() {
        return "TagSet [tags=" + stream().collect(Collectors.joining(", ")) + "]";
    }

}
