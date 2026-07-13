1. **Identify the missing test coverage:**
   - The JaCoCo reports point out significant missing instruction coverage in `GlobalNodeSet`, `GlobalEdgeSet`, `EphemeralNodeSet`, and `EphemeralEdgeSet`.
   - The missing coverage is mostly around set operations, validations, and conversions, such as `filter`, `intersect`, `difference`, `union`, `validate`, `toImmutable`, and the collection mutators (`add`, `remove`, `addAll`, `retainAll`, etc).
2. **Implement coverage tests:**
   - I have created `GlobalNodeSetCoverageTest.java`, `GlobalEdgeSetTest.java` (overriding the existing basic one), `EphemeralNodeSetCoverageTest.java`, and `EphemeralEdgeSetCoverageTest.java`.
   - I will merge these into the canonical test files (e.g. `GlobalNodeSetTest.java`, `GlobalEdgeSetTest.java`, `EphemeralNodeSetTest.java`, `EphemeralEdgeSetTest.java`).
   - I will add some more tests to reach higher coverage for the edge cases, especially related to the `filter` edge cases and `intersect`, `union`, `difference` returning `empty` vs `singleton` vs `multi`.
3. **Run Pre-Commit Checks:**
   - Run tests to ensure no regressions and verify the coverage improves.
4. **Submit:**
   - Commit and submit the code.
