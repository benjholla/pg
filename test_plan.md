1. **Add `DistributiveLawsInvariantTest` in `pg-global` module**
    - Ensure that De Morgan's laws and algebraic distributivity properties are fully covered in `pg-global`. Specifically, `intersection(A, B U C)` == `union(intersection(A, B), intersection(A, C))` and `union(A, intersection(B, C))` == `intersection(union(A, B), union(A, C))`
2. **Add `DistributiveLawsInvariantTest` in `pg-multiverse` module**
    - Replicate the same algebraic properties testing for Ephemeral graph implementation.
3. **Add `IdempotenceInvariantTest` in `pg-global` module**
    - Ensure idempotent properties are validated for standard set operations `union(A, A) == A`, and `intersection(A, A) == A`.
4. **Add `IdempotenceInvariantTest` in `pg-multiverse` module**
    - Apply identical checks to `pg-multiverse` to prevent implementation drift.
5. **Add tests for multiple vararg parameters in graph operations**
    - The `GraphAlgebraicPropertiesTest` and `AlgebraicPropertyInvariantTest` has only single method combinations and do not check edge-case interactions of algebraic functions across their `vararg` and `Node.../Edge...` counterparts. E.g. properties testing if `a.union(b).union(c) == a.union(b, c)`. Note: Some of this is already in `GraphAlgebraicPropertiesTest`. I will extract these to an `AssociativeLawsInvariantTest` and implement it for `pg-multiverse` and `pg-global`.
