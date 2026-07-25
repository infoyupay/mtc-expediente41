# Junie Coding Guidelines

These guidelines define the coding, documentation, and JavaFX conventions for this project.

## General Language Rules

Use English for all source code identifiers, comments, Javadocs, exception messages, log messages, test names, and developer-facing text.

Use Spanish only for user-facing UI messages, labels, prompts, validation messages, dialog titles, and any text that appears directly in the application interface.

Class names, method names, field names, package names, and constants must be written in English, except when an explicit domain/entity name is provided and must be preserved.

## Naming Conventions

Use `camelCase` for Java production code identifiers:

```java
private String alphaCode;
public String alphaCode() { ... }
```

Use `PascalCase` for Java class, record, enum, and interface names:

```java
CountryViewModel
FxCountryManagement
FunctionTableCellValueFactory
```

Use `UPPER_SNAKE_CASE` for constants:

```java
private static final int DEFAULT_MAX_LENGTH = 255;
```

Use `snake_case` for resource file names and resource identifiers where applicable:

```text
mx_country.fxml
op18.css
country_editor.fxml
```

Use `snake_case` for test method names:

```java
@Test
void should_return_null_when_input_is_blank() {
    ...
}
```

## Java Documentation Rules

Write Javadocs in English.

Every class, interface, enum, annotation, record, constructor, method, and inner type must have Javadocs, including private members.

Private instance fields do not require Javadocs.

Static fields must always have Javadocs, even when private.

Every constructor must have Javadocs, including private constructors.

Every `package-info.java` file must have package-level Javadocs.

Every class-level and package-level Javadoc must include:

```java
@author David Vidal - InfoYupay SACS
@version 1.0
```

Do not use `<p>` in Javadocs.

Use `<br/>` for simple line breaks between related paragraphs.

Use `<ul>`, `<ol>`, and `<li>` for lists. Do not use manual hyphenated or numbered lists inside Javadocs.

Prefer `{@code ...}` over `<code>...</code>`.

Use `{@link ...}` when referring to classes, interfaces, records, enums, annotations, methods, constructors, or fields.

Do not use `<b>` or `<i>`.

Use `<strong>` and `<em>` instead when emphasis is required.

## Required Javadoc Tags

If a class, interface, method, constructor, or record declares generic type parameters, document each one with `@param`.

Example:

```java
/**
 * Adapts row model properties for JavaFX table columns.
 *
 * @param <S> the row item type.
 * @param <T> the column value type.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class FunctionTableCellValueFactory<S, T> {
    ...
}
```

If a method or constructor has parameters, document each one with `@param`.

If a method returns a value, document it with `@return`.

If a method or constructor declares `throws` in its signature, document each declared exception with `@throws`.

If a class is a `record`, document each record component using `@param` in the class-level Javadoc.

Example:

```java
/**
 * Represents a loaded JavaFX form and its controller.
 *
 * @param root the loaded root node.
 * @param controller the controller associated with the root node.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record LoadedForm<T, C>(T root, C controller) {
}
```

## Javadoc Formatting Style

Use compact Javadocs with `<br/>` for simple separation.

Preferred:

```java
/**
 * Provides support types for configuring JavaFX {@link javafx.scene.control.TableView}
 * instances.
 * <br/>
 * This package groups utilities that help bridge application view models and the
 * callback-oriented API used by JavaFX table columns.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
```

Avoid:

```java
/**
 * Provides support types for configuring JavaFX tables.
 *
 * <p>This package groups utilities...</p>
 */
```

When a list is needed, use HTML list tags:

```java
/**
 * Typical responsibilities include:
 * <ul>
 *     <li>creating cell value factories;</li>
 *     <li>reducing reflection-based table mappings;</li>
 *     <li>keeping table configuration close to the UI layer.</li>
 * </ul>
 */
```

## JavaFX FXML Controller Rules

FXML-injected controls must always be private and annotated with `@FXML`.

Preferred:

```java
@FXML
private TableColumn<CountryViewModel, Boolean> colActive;
```

Avoid:

```java
public TableColumn<CountryViewModel, Boolean> colActive;
```

The `initialize()` method must be private and annotated with `@FXML`.

Preferred:

```java
@FXML
private void initialize() {
    ...
}
```

FXML event handlers must be private and annotated with `@FXML`.

Preferred:

```java
@FXML
private void handleCreate() {
    ...
}
```

Avoid exposing UI controls as public API.

Controllers should expose behavior through methods only when another controller or application component legitimately needs to invoke that behavior.

## JavaFX TableView Rules

Prefer explicit, type-safe cell value factories over string-based reflection mappings.

Avoid `PropertyValueFactory` when a typed alternative exists.

Prefer project-specific table cell value factory classes that can be referenced from FXML through `fx:factory`.

Example:

```xml
<cellValueFactory>
    <CountryTableCellValues fx:factory="name"/>
</cellValueFactory>
```

Entity-specific table value factory classes should centralize column-to-view-model-property mappings for catalog and maintenance screens.

Example class names:

```text
CurrencyTableCellValues
CountryTableCellValues
PeriodTableCellValues
```

Generic adapters may be used when they reduce boilerplate without hiding intent.

## JavaFX UI Text Rules

FXML labels, button text, tooltips, validation messages, dialog titles, and other user-visible strings must be written in Spanish.

Developer-facing Java names around those UI elements must remain in English.

Example:

```xml
<Label text="Mantenimiento de Países"/>
<Button text="Nuevo"/>
```

```java
@FXML
private void handleCreate() {
    ...
}
```

## Exception and Logging Rules

Exception messages must be written in English.

Log messages must be written in English.

User-facing error dialogs or validation messages must be written in Spanish.

Example:

```java
throw new IllegalStateException("Country editor form has not been loaded.");
```

```java
validator.createCheck()
        .dependsOn("name", txtName.textProperty())
        .withMethod(context -> {
            if (txtName.getText().isBlank()) {
                context.error("El nombre del país es obligatorio.");
            }
        })
        .decorates(txtName);
```

## Package Design Rules

Keep packages focused on their architectural purpose.

Do not turn utility or UI-support packages into repositories for business rules.

JavaFX support packages must remain presentation-layer helpers.

Validation helper packages may improve ergonomics over ValidatorFX, but they must not own business policy.

TableView helper packages may expose values to JavaFX tables, but they must not perform persistence, navigation, validation, or business calculations.

## Constructors

Always provide Javadocs for constructors.

FXML controllers may have explicit no-argument constructors when useful to document lifecycle constraints.

Constructor logic in FXML controllers must not access FXML-injected fields, because injection happens after construction.

Preferred:

```java
/**
 * Creates a new country management controller.
 * <br/>
 * This constructor intentionally performs no UI initialization because FXML
 * fields are injected after controller construction. Table configuration and
 * other control setup must be performed from {@link #initialize()}.
 */
public FxCountryManagement() {
}
```

## Refactoring Rules

Prefer code structures that are friendly to IDE refactoring.

Avoid string-based references when Java symbols can be used instead.

When FXML can reference Java factories or methods directly, prefer that over reflection-based property names.

Before renaming methods referenced by FXML, ensure the FXML reference is updated as part of the same refactor.

## Testing Rules

Use English for test class names, test method names, assertions, and test helper methods.

Use `snake_case` for test method names.

Test method names should describe behavior.

Preferred:

```java
@Test
void should_return_null_when_input_has_no_digits() {
    ...
}
```

Avoid:

```java
@Test
void test1() {
    ...
}
```

## Before Making Changes

Before modifying existing code:

1. Follow the established style of the surrounding package.
2. Preserve public API unless the requested change explicitly requires refactoring it.
3. Keep changes small and cohesive.
4. Avoid introducing new abstractions unless there is an actual repeated pattern.
5. Do not anticipate speculative future requirements with unnecessary framework code.
6. Prefer simple, readable implementations over clever generic machinery.
