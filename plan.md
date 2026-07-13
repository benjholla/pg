1. **Identify Missing API Contracts**: Explore and review the `NodeSet` and `EdgeSet` implementations (Immutable, UnmodifiableLive, Singletons, and Generic variants) alongside graph APIs.
2. **Apply Null Handling Guarantees**: Add `Objects.requireNonNull` validation to `union`, `intersect`, and `difference` methods that accept varargs, arrays, or collections across `pg-api`, `pg-global`, and `pg-multiverse` components.
3. **Patch `AttributeMap` Interface Default Hazards**: Override Java 8 default map compute/merge methods on the `AttributeMap` interface to throw `UnsupportedOperationException` to protect custom map type invariants from bypassing explicit `null` checking overrides.
4. **Update Null Pointer Test Coverage**: Validate NPE thresholds across the modified implementations via the specific unit test edge case files, fixing the previously failing and new test gaps.
5. **Pre-Commit Checks**: Follow the `pre_commit_instructions` tool to make sure we run checks against the full test suite and confirm formatting with `./gradlew check`.
6. **Submit Changes**: Push branch up to complete the review.
