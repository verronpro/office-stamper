# Office-Stamper Java Style Guide

This document outlines the coding standards and best practices for the Office-Stamper project. Following these
guidelines ensures consistency across the codebase and helps maintain code quality.

## Table of Contents

1. [Code Formatting](#code-formatting)
2. [Naming Conventions](#naming-conventions)
3. [Architecture and Design Patterns](#architecture-and-design-patterns)
4. [Java Language Features](#java-language-features)
5. [Documentation](#documentation)
6. [Error Handling](#error-handling)
7. [Testing](#testing)
8. [Common Pitfalls to Avoid](#common-pitfalls-to-avoid)

## Code Formatting

### Line Length

- Soft limit of 120 characters per line
- Wrap long lines according to the IntelliJ style configuration

### Indentation and Spacing

- Use 4 spaces for indentation (not tabs)
- Add a space after keywords like `if`, `for`, `while`, etc.
- Add a space before opening braces
- Add a space around operators (`+`, `-`, `*`, `/`, `=`, etc.)
- Add a space after commas in argument lists

### Braces

- Opening braces go on the same line for methods, classes, and control structures
- `else` statements go on a new line
- Keep simple blocks, methods, lambdas, and classes in one line when appropriate

### Method and Parameter Wrapping

- Method parameters should wrap with each parameter on a new line when they don't fit on one line
- Method parameters should have the opening parenthesis on the next line when wrapped
- Method parameters should have the closing parenthesis on the next line when wrapped
- Align multiline chained methods
- Binary operation signs should go on the next line when wrapped

## Naming Conventions

### Classes and Interfaces

- Use PascalCase (e.g., `DocxStamper`, `RepeatProcessor`)
- Use nouns or noun phrases for class names
- Use adjectives or descriptive phrases for interfaces (e.g., `Processable`)
- Interfaces that define a single method can be named with the suffix "er" (e.g., `Resolver`)

### Methods

- Use camelCase (e.g., `processDocument`, `addCustomFunction`)
- Use verbs or verb phrases that clearly describe the action
- Getter methods should be prefixed with "get" (e.g., `getComment`)
- Boolean getter methods should be prefixed with "is" or "has" (e.g., `isValid`, `hasContent`)
- Setter methods should be prefixed with "set" (e.g., `setConfiguration`)

### Variables and Fields

- Use camelCase (e.g., `tableRowsToRepeat`, `commentWrapper`)
- Use descriptive names that clearly indicate the purpose
- Avoid single-letter variable names except for loop counters or temporary variables with limited scope
- Prefix boolean variables with "is" or "has" when appropriate

### Constants

- Use UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- Declare constants as `static final`

## Architecture and Design Patterns

### Factory Methods

- Use static factory methods instead of constructors for complex object creation
- Name factory methods with prefixes like "new", "create", "of", etc. (e.g., `newInstance`)

### Builder Pattern

- Use the builder pattern for objects with many optional parameters
- Use method chaining for builders (e.g., `configuration.addCustomFunction(...).withImplementation(...)`)

### Dependency Injection

- Use constructor injection for required dependencies
- Make dependencies explicit in method signatures

### Utility Classes

- Make utility classes final with a private constructor that throws an exception
- Use static methods for utility functions

### Inheritance vs. Composition

- Prefer composition over inheritance when possible
- Use interfaces to define behavior
- Extend abstract classes when sharing implementation details

### Immutability

- Make classes immutable when possible
- Use final fields for immutable objects
- Return defensive copies of mutable objects

## Java Language Features

### Java Version

- Use Java 8+ features when appropriate
- Prefer lambda expressions over anonymous classes for functional interfaces
- Use method references when they improve readability (e.g., `RepeatProcessor::newInstance`)

### Collections

- Use the Collections Framework appropriately
- Prefer interfaces over implementations in variable declarations (e.g., `List<String>` instead of `ArrayList<String>`)
- Use factory methods for small collections (e.g., `List.of()`, `Map.of()`)

### Streams and Functional Programming

- Use streams for processing collections when appropriate
- Keep stream operations simple and readable
- Extract complex stream operations into separate methods
- Prefer method references over lambdas when they improve readability

### Optional

- Use Optional for return values that might be absent
- Don't use Optional for method parameters or fields
- Use Optional methods like `map`, `filter`, and `orElse` appropriately

### var Keyword

- Use `var` for local variables when the type is obvious from the context
- Avoid using `var` when it makes the code less readable

## Documentation

### Javadoc

- Add Javadoc comments for all public classes, interfaces, and methods
- Include a brief description of the purpose and behavior
- Document parameters, return values, and exceptions
- Use `@param`, `@return`, and `@throws` tags appropriately
- Add blank lines after parameter comments and return statements
- Preserve line feeds in Javadoc comments

### Code Comments

- Use comments to explain "why" not "what"
- Keep comments up-to-date with code changes
- Add a space after the comment delimiter (e.g., `// Comment` not `//Comment`)

## Error Handling

### Exceptions

- Use checked exceptions for recoverable errors
- Use unchecked exceptions for programming errors
- Create custom exceptions when appropriate (e.g., `OfficeStamperException`)
- Include meaningful error messages
- Don't catch exceptions without handling them properly
- Use try-with-resources for automatic resource management

### Null Handling

- Use `@Nullable` and `@NonNull` annotations to document nullability
- Use `Objects.requireNonNull()` to validate parameters
- Use Optional for return values that might be absent
- Avoid returning null from methods when possible

## Testing

### Unit Tests

- Write unit tests for all public methods
- Use descriptive test names that explain the scenario and expected outcome
- Follow the Arrange-Act-Assert pattern
- Keep tests independent and isolated
- Use mocks and stubs appropriately

### Test Coverage

- Aim for high test coverage, especially for core functionality
- Test edge cases and error conditions
- Test both positive and negative scenarios

## Common Pitfalls to Avoid

### Resource Management

- Always close resources properly (use try-with-resources)
- Don't rely on garbage collection for resource cleanup

### Concurrency Issues

- Be careful with shared mutable state
- Use thread-safe collections and utilities when needed
- Document thread-safety assumptions

### Performance Considerations

- Avoid premature optimization
- Profile before optimizing
- Consider memory usage for large data structures
- Be mindful of expensive operations in loops

### Code Duplication

- Don't repeat yourself (DRY principle)
- Extract common functionality into reusable methods or classes
- Use inheritance or composition to share behavior

### Overengineering

- Keep it simple
- Don't add complexity or flexibility that isn't needed
- Solve the current problem, not hypothetical future problems
