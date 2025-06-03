# Phase 3: Java 17 → Java 21 Migration

## Objectives
- Upgrade Java 17 → Java 21 (LTS)
- Update dependencies for Java 21 compatibility
- Adopt safe Java 21 features where beneficial
- Maintain 100% API compatibility
- Optimize performance with Java 21 improvements

## Prerequisites
- Phase 2 successfully completed
- Java 17 build and tests working perfectly
- Spring Boot 3.x operational
- All API contracts validated
- Performance baseline from Java 17 established

## Steps

### Step 3.1: Java 21 Configuration

#### Step 3.1.1: Update Maven Java Version
**Step ID**: 3.1.1
**Description**: Update Maven configuration for Java 21
**Commands**:
```bash
# Backup current pom.xml
cp pom.xml pom.xml.java17.backup

# Check current Java configuration
grep -A 5 -B 5 "java.version\|maven.compiler" pom.xml
```
**Action**: Update java.version, maven.compiler.source, maven.compiler.target to 21
**Expected Result**: pom.xml configured for Java 21
**Validation**: `grep "java.version\|maven.compiler" pom.xml` shows version 21
**On Success**: Move to step 3.1.2
**On Failure**: Fix pom.xml configuration syntax

#### Step 3.1.2: Update Maven Compiler Plugin
**Step ID**: 3.1.2
**Description**: Ensure Maven compiler plugin supports Java 21
**Commands**:
```bash
# Check compiler plugin version
grep -A 10 "maven-compiler-plugin" pom.xml
```
**Action**: Update maven-compiler-plugin to latest version supporting Java 21
**Expected Result**: Compiler plugin compatible with Java 21
**Validation**: `mvn help:describe -Dplugin=compiler` confirms Java 21 support
**On Success**: Move to step 3.2.1
**On Failure**: Update plugin to compatible version

### Step 3.2: Dependency Updates

#### Step 3.2.1: Update Spring Boot Version
**Step ID**: 3.2.1
**Description**: Update Spring Boot to latest version supporting Java 21
**Commands**:
```bash
# Check current Spring Boot version
grep -A 5 "spring-boot-starter-parent" pom.xml

# Check Spring Boot Java 21 compatibility
mvn dependency:tree | grep spring-boot | head -5
```
**Action**: Update Spring Boot to latest 3.x version with Java 21 support
**Expected Result**: Spring Boot version supports Java 21
**Validation**: Spring Boot documentation confirms Java 21 compatibility
**On Success**: Move to step 3.2.2
**On Failure**: Find suitable Spring Boot version for Java 21

#### Step 3.2.2: Update Testing Framework Dependencies
**Step ID**: 3.2.2
**Description**: Update test dependencies for Java 21 compatibility
**Commands**:
```bash
# Check test dependency versions
mvn dependency:tree | grep -E "junit|mockito|hamcrest"
```
**Action**: Update JUnit, Mockito, and other test dependencies
**Expected Result**: All test dependencies support Java 21
**Validation**: Test dependencies have Java 21 compatibility confirmed
**On Success**: Move to step 3.2.3
**On Failure**: Find Java 21 compatible test dependency versions

#### Step 3.2.3: Update Build Plugin Versions
**Step ID**: 3.2.3
**Description**: Update all Maven plugins for Java 21 compatibility
**Commands**:
```bash
# Check all plugin versions
grep -A 3 -B 1 "<plugin>" pom.xml | grep -A 2 "version"
```
**Action**: Update maven-surefire-plugin, jacoco-maven-plugin, and others
**Expected Result**: All plugins support Java 21
**Validation**: No plugin compatibility warnings during build
**On Success**: Move to step 3.3.1
**On Failure**: Find Java 21 compatible plugin versions

### Step 3.3: Code Modernization (Optional Safe Features)

#### Step 3.3.1: Evaluate Pattern Matching Opportunities
**Step ID**: 3.3.1
**Description**: Identify safe pattern matching improvements
**Commands**:
```bash
# Find instanceof patterns that could benefit from pattern matching
grep -r "instanceof" src/main/java/ | head -10
```
**Action**: Only apply pattern matching where it doesn't affect API contracts
**Expected Result**: Code improvements that maintain API compatibility
**Validation**: API contract tests still pass after changes
**On Success**: Move to step 3.3.2
**On Failure**: Revert pattern matching changes

#### Step 3.3.2: Evaluate Text Blocks for Multi-line Strings
**Step ID**: 3.3.2
**Description**: Replace complex multi-line strings with text blocks where safe
**Commands**:
```bash
# Find multi-line string concatenations
grep -r "\"\s*+\s*$" src/main/java/ | head -5
```
**Action**: Replace with text blocks only in internal code, not API responses
**Expected Result**: Improved readability without API changes
**Validation**: API responses unchanged, tests pass
**On Success**: Move to step 3.4.1
**On Failure**: Revert text block changes

### Step 3.4: Testing and Validation

#### Step 3.4.1: Build Validation on Java 21
**Step ID**: 3.4.1
**Description**: Verify project builds successfully on Java 21
**Commands**:
```bash
mvn clean compile
```
**Expected Result**: Build compiles successfully on Java 21
**Validation**: BUILD SUCCESS with no compilation errors or warnings
**On Success**: Move to step 3.4.2
**On Failure**: Fix Java 21 compilation issues

#### Step 3.4.2: Complete Test Suite Execution
**Step ID**: 3.4.2
**Description**: Run all tests on Java 21
**Commands**:
```bash
mvn test
```
**Expected Result**: All tests pass (100% success rate)
**Validation**: No test failures, errors, or timeouts
**On Success**: Move to step 3.4.3
**On Failure**: Fix failing tests, check Java 21 behavior changes

#### Step 3.4.3: Coverage Maintenance Validation
**Step ID**: 3.4.3
**Description**: Verify test coverage maintained on Java 21
**Commands**:
```bash
mvn jacoco:report
```
**Expected Result**: Coverage ≥80% maintained or improved
**Validation**: Coverage report shows no decrease from Java 17
**On Success**: Move to step 3.4.4
**On Failure**: Add tests for any coverage gaps

#### Step 3.4.4: API Compatibility Verification
**Step ID**: 3.4.4
**Description**: Verify API contracts unchanged on Java 21
**Commands**:
```bash
# Start application on Java 21
mvn spring-boot:run &
APP_PID=$!
sleep 15

# Test all critical endpoints
curl -s http://localhost:8080/api/users | jq . > java21_users_response.json
curl -s http://localhost:8080/api/products | jq . > java21_products_response.json

# Compare with Java 11 baseline responses
# diff java11_users_response.json java21_users_response.json

kill $APP_PID
```
**Expected Result**: API responses identical to Java 11/17 baseline
**Validation**: Response formats, status codes, headers unchanged
**On Success**: Move to step 3.4.5
**On Failure**: Fix API compatibility issues

#### Step 3.4.5: Performance Optimization Validation
**Step ID**: 3.4.5
**Description**: Measure Java 21 performance improvements
**Commands**:
```bash
# Measure build performance
time mvn clean compile

# Measure test execution performance
time mvn test

# Measure application startup performance
time mvn spring-boot:run &
APP_PID=$!
sleep 15

# Measure memory usage
jps | grep Application
jstat -gc $APP_PID

kill $APP_PID
```
**Expected Result**: Performance equal or better than Java 17 baseline
**Validation**: Startup time, memory usage, build time comparisons
**On Success**: Move to step 3.5.1
**On Failure**: Investigate performance regression

### Step 3.5: Final Phase Validation

#### Step 3.5.1: Integration Testing on Java 21
**Step ID**: 3.5.1
**Description**: Run complete integration test suite
**Commands**:
```bash
# Run all integration tests
mvn test -Dtest="*IntegrationTest,*IT"

# Run client simulation tests
mvn test -Dtest="*ClientSimulationTest"
```
**Expected Result**: All integration tests pass
**Validation**: Client simulation behaves identically to previous Java versions
**On Success**: Move to step 3.5.2
**On Failure**: Fix integration test failures

#### Step 3.5.2: Load Testing Validation
**Step ID**: 3.5.2
**Description**: Validate performance under load on Java 21
**Commands**:
```bash
# Start application
mvn spring-boot:run &
APP_PID=$!
sleep 15

# Simple load test
for i in {1..50}; do
  curl -s http://localhost:8080/api/users > /dev/null &
  curl -s http://localhost:8080/api/products > /dev/null &
done
wait

# Check memory after load
jstat -gc $APP_PID

kill $APP_PID
```
**Expected Result**: Application handles load without degradation
**Validation**: Memory usage stable, response times consistent
**On Success**: Phase 3 complete, update progress.md to phase_4_java22
**On Failure**: Investigate load handling issues

## Phase Completion Criteria
- [ ] Java version successfully upgraded to 21
- [ ] All dependencies support Java 21
- [ ] All tests pass (100% success rate)
- [ ] Coverage ≥80% maintained
- [ ] API contracts identical to previous versions
- [ ] Performance equal or better than Java 17
- [ ] Integration tests validate all functionality
- [ ] No client-breaking changes

## Troubleshooting
See `spec/java22_upgrade/common/troubleshooting.md` for:
- Java 21 compilation issues
- Dependency compatibility problems
- Performance regression investigation
- Modern Java feature integration issues

## Next Phase
Upon completion, move to Phase 4: Java 21 → Java 22 Final Migration
