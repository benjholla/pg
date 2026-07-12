# Contributing to pg

Thank you for your interest in contributing to `pg`! This document provides guidelines for setting up your environment, making changes, and submitting pull requests.

## Development Environment Setup

This project uses Gradle as its build system and requires Java 17. Please ensure you have a Java 17 JDK installed.

## Development Commands

*   **Verify compilation:**
    ```bash
    ./gradlew classes
    ```
*   **Run tests:**
    ```bash
    ./gradlew test
    ```
*   **Generate test coverage report:**
    ```bash
    ./gradlew jacocoTestReport
    ```
*   **Generate documentation:**
    ```bash
    ./gradlew javadoc
    ```

## Testing Philosophy (Bouncer)

We prioritize repository confidence, correctness, strict invariants, explicit specifications, behavioral verification, and deterministic tests. Treat broken or disabled tests as high-priority technical debt.

*   Always run `./gradlew test` and ensure all tests pass before opening a PR.
*   If you add a new feature or fix a bug, please write tests that verify the expected behavior rather than just confirming implementation details.

## Documentation Philosophy (Steward)

Documentation is part of the product and should evolve with the code.

*   Ensure the documented capabilities accurately reflect the implementation.
*   Whenever documentation contradicts implementation, implementation wins. Update the documentation.
*   Every public API must be documented using Javadoc.

## Pull Request Guidelines

1.  Read all relevant documentation and `AGENTS.md` to understand the project's architecture and vision.
2.  Review existing open Pull Requests to avoid duplicating work.
3.  Write tests for new functionality and bug fixes.
4.  Run all development commands (tests, compilation check) locally.
5.  Provide a clear, descriptive PR title and summary of changes.
