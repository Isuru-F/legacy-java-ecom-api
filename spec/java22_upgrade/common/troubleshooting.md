# Common Troubleshooting Guide

## Build Failures

### Maven Compilation Issues
**Symptom**: `mvn clean compile` fails
**Common Causes**:
- Java version mismatch
- Missing dependencies
- Syntax errors

**Resolution Steps**:
1. Check Java version: `java -version`
2. Check Maven version: `mvn -version`
3. Review error output for specific issues
4. Check pom.xml for dependency conflicts

### Test Failures
**Symptom**: `mvn test` has failures
**Resolution Steps**:
1. Run single failing test: `mvn test -Dtest=TestClassName`
2. Check test logs in target/surefire-reports/
3. Fix test issues before proceeding
4. Re-run full test suite

## Coverage Issues

### JaCoCo Plugin Not Found
**Symptom**: `mvn jacoco:report` fails with plugin not found
**Resolution**:
```xml
<!-- Add to pom.xml <plugins> section -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Low Coverage Issues
**Symptom**: Coverage below 80% threshold
**Resolution Strategy**:
1. Identify uncovered classes: Check target/site/jacoco/index.html
2. Focus on high-impact areas first (services, controllers)
3. Add missing test cases incrementally
4. Re-run coverage after each addition

## Spring Boot Issues

### Application Won't Start
**Symptom**: `mvn spring-boot:run` fails
**Common Causes**:
- Port already in use
- Configuration issues
- Missing dependencies

**Resolution Steps**:
1. Check port availability: `lsof -i :8080`
2. Review application logs
3. Check application.properties configuration
4. Verify all dependencies are present

### javax → jakarta Migration Issues
**Symptom**: Import errors during Spring Boot 2.x → 3.x upgrade
**Resolution Pattern**:
```bash
# Find all javax.persistence imports
grep -r "javax.persistence" src/

# Replace with jakarta.persistence
find src/ -name "*.java" -exec sed -i 's/javax.persistence/jakarta.persistence/g' {} \;
```

## Database Issues

### H2 Database Connection Failures
**Symptom**: Tests fail with database connection errors
**Resolution Steps**:
1. Check H2 console accessibility: http://localhost:8080/api/h2-console
2. Verify JDBC URL: `jdbc:h2:mem:testdb`
3. Check username: `sa`, password: (blank)
4. Review application-test.properties

## Dependency Conflicts

### Version Conflicts
**Symptom**: Maven reports dependency conflicts
**Resolution Steps**:
1. Run dependency tree: `mvn dependency:tree`
2. Identify conflicting versions
3. Add explicit dependency management in pom.xml
4. Use Maven's `<dependencyManagement>` section

### Missing Dependencies
**Symptom**: ClassNotFoundException at runtime
**Resolution Steps**:
1. Identify missing class package
2. Search Maven Central for appropriate dependency
3. Add to pom.xml with compatible version
4. Re-run tests to verify fix

## Performance Issues

### Slow Build Times
**Resolution Steps**:
1. Use Maven parallel builds: `mvn -T 4 clean compile`
2. Skip tests for compile-only: `mvn clean compile -DskipTests`
3. Use incremental compilation where possible

### Slow Test Execution
**Resolution Steps**:
1. Run tests in parallel: Configure surefire plugin
2. Use H2 in-memory for faster test database
3. Mock external dependencies
4. Profile slow tests individually

## Recovery Procedures

### When to Reset to Known State
- Multiple consecutive step failures
- Unexpected system behavior
- Corrupted build artifacts

### Reset Commands
```bash
# Clean everything
mvn clean
rm -rf target/

# Reset to git clean state
git status
git checkout -- .
git clean -fd

# Rebuild from scratch
mvn clean compile test
```

## Escalation Criteria
Request human intervention when:
- Same step fails 3+ times with different approaches
- External dependency issues (network, repositories)
- Complex configuration conflicts requiring architectural decisions
- Time-sensitive blockers affecting delivery timeline
