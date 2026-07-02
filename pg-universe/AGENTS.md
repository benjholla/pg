# `pg-universe` Architectural Rules

* `pg-universe` MUST NEVER depend on `pg-heavy`.
* Rationale: Introducing a dependency on `pg-heavy` would collapse the architectural boundary between the two modules and severely limit the capabilities of the transactional engine. `pg-universe` and `pg-heavy` must remain strictly parallel siblings that share no code other than the contracts defined in `pg-api`.