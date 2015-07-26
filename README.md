# SpEL maven plugin
This plugin scans your class files looking for the configured annotations and then validating any Spring Expressions that you have in the annotations.

#### What is this plugin meant to do?
- Find syntax errors in expressions that you might not otherwise find until runtime
- Basic static analysis on the expressions to see if they're correcting invoke a context root's method.

#### Types of validation
- Parse the expression into a SpelExpression
- Optionally validate any top level method references to the configured context root.
- *Does not validate* Bean References `@myBean.foo` or Variables `#foo.bar` and likely won't ever
- *Does not currently validate* Type References `T(fully.qualified.name.Type).isFoo()` but these seem like a good candidate to add next.

#### How it works
- The plugin uses the classpath for the project in order to load the configured annotations and optional root contexts.
- The Reflections API https://github.com/ronmamo/reflections is used to locate all methods annotated with the configured annotations
- Each annotation is checked to see if its configured attribute has a string value
- The string value is parsed into a SpelExpression or recorded as an error it it fails.
- If a context root was configured for the annotation, then we'll also scan the SpelExpression for any top level method references to see if they resolve against the root.
- A summary of the number of annotations tested, passed, and failed is available in target/spel-maven-plugin/report.json.

## Example

### Validate Spring's PreAuthorize annotation
```xml
  <plugin>
      <groupId>com.massfords</groupId>
      <artifactId>spel-maven-plugin</artifactId>
      <version>1.0</version>
      <configuration>
          <failOnError>false</failOnError>
          <annotations>
              <SpelAnnotation>
                  <name>org.springframework.security.access.prepost.PreAuthorize</name>
                  <expressionRoot>org.springframework.security.access.expression.SecurityExpressionRoot</expressionRoot>
              </SpelAnnotation>
          </annotations>
      </configuration>
      <executions>
          <execution>
              <id>spel-plugin-integration</id>
              <phase>process-classes</phase>
              <goals>
                  <goal>spel</goal>
              </goals>
          </execution>
      </executions>
  </plugin>
```

## To build this project:
`mvn clean install`

## To build this project with plugin integration tests
`mvn clean install -Prun-its`

## Issues, Contributing
Please post any issues on the Github's Issue tracker. Pull requests are welcome!

### License
Apache 2.0
