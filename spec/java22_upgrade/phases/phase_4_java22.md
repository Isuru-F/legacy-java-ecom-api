# Phase 4: Java 21 → Java 22 Final Migration

## Objectives
- Complete upgrade to Java 22 (latest stable)
- Finalize all dependency updates
- Adopt safe Java 22 features where beneficial
- Validate production readiness
- Complete comprehensive testing and documentation

## Prerequisites
- Phase 3 successfully completed
- Java 21 build and tests working perfectly
- All API contracts validated on Java 21
- Performance baseline established on Java 21
- Load testing completed successfully

## Steps

### Step 4.1: Java 22 Configuration

#### Step 4.1.1: Update Maven Configuration for Java 22
**Step ID**: 4.1.1
**Description**: Update Maven configuration to Java 22
**Commands**:
```bash
# Backup current pom.xml
cp pom.xml pom.xml.java21.backup

# Update Java version configuration
```
**Action**: Update java.version, maven.compiler.source, maven.compiler.target to 22
**Expected Result**: pom.xml configured for Java 22
**Validation**: `grep -E "java.version|maven.compiler" pom.xml` shows version 22
**On Success**: Move to step 4.1.2
**On Failure**: Fix pom.xml configuration issues

#### Step 4.1.2: Update IDE Configuration
**Step ID**: 4.1.2
**Description**: Update IDE/development environment for Java 22
**Commands**:
```bash
# Verify Java 22 installation
java -version
javac -version
```
**Expected Result**: Java 22 properly installed and configured
**Validation**: Both java and javac report version 22
**On Success**: Move to step 4.2.1
**On Failure**: Install/configure Java 22 properly

### Step 4.2: Final Dependency Updates

#### Step 4.2.1: Update Spring Boot to Latest Java 22 Compatible Version
**Step ID**: 4.2.1
**Description**: Update Spring Boot to latest version with Java 22 support
**Commands**:
```bash
# Check current Spring Boot version
grep "spring-boot-starter-parent" pom.xml

# Verify Java 22 compatibility
mvn dependency:tree | grep spring-boot | head -3
```
**Action**: Update to latest Spring Boot version supporting Java 22
**Expected Result**: Spring Boot fully compatible with Java 22
**Validation**: No compatibility warnings in build output
**On Success**: Move to step 4.2.2
**On Failure**: Find suitable Spring Boot version for Java 22

#### Step 4.2.2: Update All Dependencies to Latest Stable
**Step ID**: 4.2.2
**Description**: Update all dependencies to latest Java 22 compatible versions
**Commands**:
```bash
# Check for dependency updates
mvn versions:display-dependency-updates

# Update security-related dependencies
mvn dependency:tree | grep -E "jackson|security|validation"
```
**Action**: Update all dependencies to latest secure, stable versions
**Expected Result**: All dependencies at latest compatible versions
**Validation**: No known security vulnerabilities in dependencies
**On Success**: Move to step 4.2.3
**On Failure**: Resolve dependency conflicts

#### Step 4.2.3: Final Build Plugin Updates
**Step ID**: 4.2.3
**Description**: Update all Maven plugins to latest Java 22 compatible versions
**Commands**:
```bash
# Check plugin updates
mvn versions:display-plugin-updates

# Verify critical plugins
grep -A 5 -B 2 "maven-compiler-plugin\|maven-surefire-plugin\|jacoco-maven-plugin" pom.xml
```
**Action**: Update all build plugins to latest stable versions
**Expected Result**: All plugins support Java 22 with latest features
**Validation**: Build runs without plugin compatibility warnings
**On Success**: Move to step 4.3.1
**On Failure**: Fix plugin compatibility issues

### Step 4.3: Java 22 Features Integration (Safe Adoption)

#### Step 4.3.1: Evaluate Unnamed Variables and Patterns
**Step ID**: 4.3.1
**Description**: Safely adopt unnamed variables where appropriate
**Commands**:
```bash
# Find unused variables that could be unnamed
grep -r "catch.*Exception.*e)" src/main/java/ | head -5
```
**Action**: Replace unused exception variables with underscore where safe
**Expected Result**: Code improvements without API contract changes
**Validation**: All tests pass, API responses unchanged
**On Success**: Move to step 4.3.2
**On Failure**: Revert unnamed variable changes

#### Step 4.3.2: Enhanced Switch Expressions
**Step ID**: 4.3.2
**Description**: Adopt improved switch expressions where safe
**Commands**:
```bash
# Find switch statements that could be improved
grep -r -A 5 "switch.*{" src/main/java/ | head -10
```
**Action**: Improve switch statements only in internal logic, not API-facing code
**Expected Result**: Code modernization without external impact
**Validation**: API contracts unchanged, tests pass
**On Success**: Move to step 4.4.1
**On Failure**: Revert switch expression changes

### Step 4.4: Comprehensive Testing and Validation

#### Step 4.4.1: Build Validation on Java 22
**Step ID**: 4.4.1
**Description**: Verify complete build success on Java 22
**Commands**:
```bash
mvn clean compile
```
**Expected Result**: Build compiles successfully on Java 22
**Validation**: BUILD SUCCESS, no errors or warnings
**On Success**: Move to step 4.4.2
**On Failure**: Fix Java 22 compilation issues

#### Step 4.4.2: Complete Test Suite Execution
**Step ID**: 4.4.2
**Description**: Run entire test suite on Java 22
**Commands**:
```bash
mvn test
```
**Expected Result**: All tests pass (100% success rate)
**Validation**: Zero test failures, errors, or timeouts
**On Success**: Move to step 4.4.3
**On Failure**: Fix failing tests

#### Step 4.4.3: Coverage Validation and Reporting
**Step ID**: 4.4.3
**Description**: Generate final coverage report on Java 22
**Commands**:
```bash
mvn jacoco:report
```
**Expected Result**: Coverage ≥80% maintained or improved
**Validation**: Coverage meets or exceeds all previous versions
**On Success**: Move to step 4.4.4
**On Failure**: Add tests to meet coverage requirements

#### Step 4.4.4: Final API Contract Validation
**Step ID**: 4.4.4
**Description**: Verify API contracts byte-for-byte identical to Java 11 baseline
**Commands**:
```bash
# Start application on Java 22
mvn spring-boot:run &
APP_PID=$!
sleep 15

# Test all endpoints and save responses
curl -s -D headers_users.txt http://localhost:8080/api/users | jq . > java22_users_response.json
curl -s -D headers_products.txt http://localhost:8080/api/products | jq . > java22_products_response.json

# Test error scenarios
curl -s http://localhost:8080/api/users/nonexistent | jq . > java22_error_response.json

kill $APP_PID
```
**Expected Result**: All API responses identical to Java 11 baseline
**Validation**: Response content, headers, status codes match exactly
**On Success**: Move to step 4.4.5
**On Failure**: Fix API compatibility issues

#### Step 4.4.5: Performance Final Validation
**Step ID**: 4.4.5
**Description**: Comprehensive performance testing on Java 22
**Commands**:
```bash
# Build performance
time mvn clean compile

# Test performance
time mvn test

# Startup performance
time mvn spring-boot:run &
APP_PID=$!
sleep 15

# Memory usage under load
for i in {1..100}; do
  curl -s http://localhost:8080/api/users > /dev/null &
done
wait

jstat -gc $APP_PID

kill $APP_PID
```
**Expected Result**: Performance equal or better than Java 11 baseline
**Validation**: All metrics within 5% improvement of original baseline
**On Success**: Move to step 4.5.1
**On Failure**: Investigate performance issues

### Step 4.5: Production Readiness Validation

#### Step 4.5.1: Extended Integration Testing
**Step ID**: 4.5.1
**Description**: Run comprehensive integration test suite
**Commands**:
```bash
# All integration tests
mvn test -Dtest="*IntegrationTest,*IT,*ClientSimulationTest"

# Extended scenario testing
mvn test -Dtest="*ScenarioTest" 2>/dev/null || echo "No scenario tests found"
```
**Expected Result**: All integration scenarios pass
**Validation**: Client simulation tests validate real-world usage
**On Success**: Move to step 4.5.2
**On Failure**: Fix integration test failures

#### Step 4.5.2: Load Testing and Stress Testing
**Step ID**: 4.5.2
**Description**: Validate production-like load handling
**Commands**:
```bash
# Start application
mvn spring-boot:run &
APP_PID=$!
sleep 15

# Extended load test
for i in {1..200}; do
  curl -s http://localhost:8080/api/users > /dev/null &
  curl -s http://localhost:8080/api/products > /dev/null &
  if [ $((i % 50)) -eq 0 ]; then
    echo "Completed $i requests"
    jstat -gc $APP_PID | tail -1
  fi
done
wait

# Final memory check
jstat -gc $APP_PID

kill $APP_PID
```
**Expected Result**: Application handles extended load gracefully
**Validation**: Memory stable, response times consistent, no errors
**On Success**: Move to step 4.5.3
**On Failure**: Investigate load handling issues

#### Step 4.5.3: Security Validation
**Step ID**: 4.5.3
**Description**: Run security validation on Java 22
**Commands**:
```bash
# Check for security vulnerabilities
mvn dependency:tree | grep -E "jackson|spring|security" > dependencies_security_check.txt

# Verify no known vulnerabilities
# This would typically integrate with security scanning tools
echo "Manual security review required for dependencies_security_check.txt"
```
**Expected Result**: No known security vulnerabilities
**Validation**: All dependencies at secure versions
**On Success**: Move to step 4.6.1
**On Failure**: Update dependencies to fix security issues

### Step 4.6: Final Documentation and Completion

#### Step 4.6.1: Update Project Documentation
**Step ID**: 4.6.1
**Description**: Update all project documentation for Java 22
**Commands**:
```bash
# Update README.md
grep -n "Java.*11\|Java.*17\|Java.*21" README.md

# Update AGENT.md
grep -n "Java.*version" AGENT.md
```
**Action**: Update all documentation to reflect Java 22 requirements
**Expected Result**: Documentation accurately reflects Java 22 setup
**Validation**: Documentation review shows Java 22 throughout
**On Success**: Move to step 4.6.2
**On Failure**: Complete documentation updates

#### Step 4.6.2: Final Progress Update
**Step ID**: 4.6.2
**Description**: Update progress.md with complete migration summary
**Commands**:
```bash
# Record final metrics
echo "Java 22 migration completed on $(date)" >> migration_summary.txt
```
**Action**: Update progress.md with comprehensive completion summary
**Expected Result**: Complete migration timeline and metrics documented
**Validation**: Progress.md shows all phases completed
**On Success**: Project complete - Java 22 upgrade successful
**On Failure**: Complete final documentation

## Phase Completion Criteria
- [ ] Java version successfully upgraded to 22
- [ ] All dependencies at latest secure versions
- [ ] All tests pass (100% success rate)
- [ ] Coverage ≥80% maintained throughout entire migration
- [ ] API contracts byte-for-byte identical to Java 11 baseline
- [ ] Performance equal or better than Java 11 baseline
- [ ] Load testing validates production readiness
- [ ] Security validation shows no vulnerabilities
- [ ] Documentation updated for Java 22
- [ ] Complete migration timeline documented

## Troubleshooting
See `spec/java22_upgrade/common/troubleshooting.md` for:
- Java 22 specific issues
- Final dependency conflicts
- Production readiness concerns
- Performance optimization

## Project Completion
Upon completion of this phase:
1. Mark entire upgrade project as completed in progress.md
2. Create comprehensive pull request with all changes
3. Document lessons learned and process improvements
4. Plan celebration of zero-breaking-change migration! 🎉
