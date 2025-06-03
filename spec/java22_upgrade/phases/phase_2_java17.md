# Phase 2: Java 11 → Java 17 Migration + Spring Boot 3.x

## Objectives  
- Upgrade Java 11 → Java 17 (LTS)
- Migrate Spring Boot 2.7.18 → 3.x
- Handle javax → jakarta namespace migration
- Maintain 100% API compatibility
- Ensure zero client-breaking changes

## Prerequisites
- Phase 1 successfully completed
- All tests passing (100% success rate)
- Coverage ≥80% maintained
- API contracts documented and validated
- Performance baseline captured

## Steps

### Step 2.1: Dependency Analysis and Planning

#### Step 2.1.1: Java 17 Compatibility Analysis
**Step ID**: 2.1.1
**Description**: Analyze current dependencies for Java 17 compatibility
**Commands**:
```bash
# Check current Java version
java -version

# Analyze Maven dependencies
mvn dependency:tree > dependency_analysis.txt

# Check for deprecated APIs
grep -r "deprecated" src/main/java/
```
**Expected Result**: Current dependency state documented
**Validation**: Dependency analysis saved to dependency_analysis.txt
**On Success**: Move to step 2.1.2
**On Failure**: Document any critical deprecated API usage

#### Step 2.1.2: Spring Boot 3.x Migration Planning
**Step ID**: 2.1.2
**Description**: Plan Spring Boot 2.x → 3.x migration strategy
**Commands**:
```bash
# Check current Spring Boot version
grep "spring-boot" pom.xml

# Identify javax.* imports that need jakarta migration
grep -r "javax\.persistence\|javax\.validation\|javax\.servlet" src/
```
**Expected Result**: Migration plan documented with required changes
**Validation**: List of javax → jakarta changes identified
**On Success**: Move to step 2.2.1
**On Failure**: Document complex migration requirements

### Step 2.2: Maven Configuration Update

#### Step 2.2.1: Update Java Version Configuration
**Step ID**: 2.2.1
**Description**: Update Maven configuration for Java 17
**Commands**:
```bash
# Backup current pom.xml
cp pom.xml pom.xml.java11.backup

# Update Java version in pom.xml
```
**Action**: Update java.version, maven.compiler.source, maven.compiler.target to 17
**Expected Result**: pom.xml configured for Java 17
**Validation**: `grep -A 5 -B 5 "java.version\|maven.compiler" pom.xml`
**On Success**: Move to step 2.2.2
**On Failure**: Fix pom.xml syntax issues

#### Step 2.2.2: Update Maven Compiler Plugin
**Step ID**: 2.2.2
**Description**: Update maven-compiler-plugin to Java 17 compatible version
**Commands**:
```bash
# Check current compiler plugin version
grep -A 10 "maven-compiler-plugin" pom.xml
```
**Action**: Update to maven-compiler-plugin 3.11.0 or newer
**Expected Result**: Compiler plugin compatible with Java 17
**Validation**: `mvn help:describe -Dplugin=compiler` shows Java 17 support
**On Success**: Move to step 2.3.1
**On Failure**: Check plugin compatibility requirements

### Step 2.3: Spring Boot Upgrade

#### Step 2.3.1: Update Spring Boot Version
**Step ID**: 2.3.1
**Description**: Upgrade Spring Boot to 3.x version
**Commands**:
```bash
# Update Spring Boot parent version in pom.xml
grep -A 5 -B 5 "spring-boot-starter-parent" pom.xml
```
**Action**: Update Spring Boot version from 2.7.18 to 3.0.x
**Expected Result**: Spring Boot 3.x configured in pom.xml
**Validation**: `mvn dependency:tree | grep spring-boot` shows 3.x versions
**On Success**: Move to step 2.3.2
**On Failure**: Check Spring Boot 3.x compatibility requirements

#### Step 2.3.2: Handle javax → jakarta Migration
**Step ID**: 2.3.2
**Description**: Migrate all javax imports to jakarta
**Commands**:
```bash
# Find all javax imports that need migration
find src/ -name "*.java" -exec grep -l "javax\.persistence\|javax\.validation" {} \;

# Replace javax.persistence with jakarta.persistence
find src/ -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;

# Replace javax.validation with jakarta.validation  
find src/ -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
```
**Expected Result**: All javax imports replaced with jakarta
**Validation**: `grep -r "javax\.persistence\|javax\.validation" src/` returns empty
**On Success**: Move to step 2.3.3
**On Failure**: Manually fix remaining javax imports

#### Step 2.3.3: Update Spring Security Configuration
**Step ID**: 2.3.3
**Description**: Update Spring Security for 3.x compatibility
**Commands**:
```bash
# Find Spring Security configurations
find src/ -name "*.java" -exec grep -l "WebSecurityConfigurerAdapter\|@EnableWebSecurity" {} \;
```
**Action**: Update deprecated Spring Security patterns for 3.x
**Expected Result**: Spring Security configuration compatible with 3.x
**Validation**: No deprecated security configuration warnings
**On Success**: Move to step 2.4.1
**On Failure**: Fix security configuration compatibility issues

### Step 2.4: Dependency Updates

#### Step 2.4.1: Update Jackson Dependencies
**Step ID**: 2.4.1
**Description**: Update Jackson for Java 17 and Spring Boot 3.x compatibility
**Commands**:
```bash
# Check Jackson versions
mvn dependency:tree | grep jackson
```
**Expected Result**: Jackson versions compatible with Java 17 and Spring Boot 3.x
**Validation**: No Jackson version conflicts in dependency tree
**On Success**: Move to step 2.4.2
**On Failure**: Resolve Jackson version conflicts

#### Step 2.4.2: Update Apache Commons Dependencies
**Step ID**: 2.4.2
**Description**: Replace deprecated commons-collections with commons-collections4
**Commands**:
```bash
# Check current commons versions
mvn dependency:tree | grep commons
```
**Action**: Replace commons-collections 3.2.2 with commons-collections4
**Expected Result**: Modern Apache Commons dependencies
**Validation**: `mvn dependency:tree | grep commons-collections` shows version 4.x
**On Success**: Move to step 2.4.3
**On Failure**: Fix commons dependency conflicts

#### Step 2.4.3: Update H2 Database Version
**Step ID**: 2.4.3
**Description**: Update H2 database to Java 17 compatible version
**Commands**:
```bash
# Check current H2 version
mvn dependency:tree | grep h2
```
**Action**: Update H2 to latest Java 17 compatible version
**Expected Result**: H2 database compatible with Java 17
**Validation**: H2 database connections work in tests
**On Success**: Move to step 2.5.1
**On Failure**: Fix H2 compatibility issues

### Step 2.5: Testing and Validation

#### Step 2.5.1: Initial Build Validation
**Step ID**: 2.5.1
**Description**: Test build compilation on Java 17
**Commands**:
```bash
mvn clean compile
```
**Expected Result**: Build compiles successfully on Java 17
**Validation**: BUILD SUCCESS message, no compilation errors
**On Success**: Move to step 2.5.2
**On Failure**: Fix compilation errors, check troubleshooting.md

#### Step 2.5.2: Test Suite Execution
**Step ID**: 2.5.2
**Description**: Run complete test suite on Java 17
**Commands**:
```bash
mvn test
```
**Expected Result**: All tests pass (100% success rate)
**Validation**: Tests run without failures or errors
**On Success**: Move to step 2.5.3
**On Failure**: Fix failing tests, check javax/jakarta migration issues

#### Step 2.5.3: Coverage Validation
**Step ID**: 2.5.3
**Description**: Verify test coverage maintained after migration
**Commands**:
```bash
mvn jacoco:report
```
**Expected Result**: Coverage ≥80% maintained
**Validation**: Coverage report shows no significant decrease
**On Success**: Move to step 2.5.4
**On Failure**: Add tests for any coverage gaps

#### Step 2.5.4: API Contract Validation
**Step ID**: 2.5.4
**Description**: Verify API contracts unchanged after migration
**Commands**:
```bash
# Start application on Java 17
mvn spring-boot:run &
APP_PID=$!
sleep 15

# Test all endpoints return expected responses
curl -s http://localhost:8080/api/users | jq .
curl -s http://localhost:8080/api/products | jq .

# Stop application
kill $APP_PID
```
**Expected Result**: API responses identical to Java 11 baseline
**Validation**: Response formats match documented contracts
**On Success**: Move to step 2.5.5
**On Failure**: Fix API compatibility issues

#### Step 2.5.5: Performance Regression Testing
**Step ID**: 2.5.5
**Description**: Verify performance within 5% of Java 11 baseline
**Commands**:
```bash
# Measure build time
time mvn clean compile

# Measure test execution time  
time mvn test

# Measure startup time
time mvn spring-boot:run &
APP_PID=$!
sleep 15
kill $APP_PID
```
**Expected Result**: Performance within 5% of Java 11 baseline
**Validation**: Times compared to Phase 1 baseline measurements
**On Success**: Phase 2 complete, update progress.md to phase_3_java21
**On Failure**: Investigate performance regression

## Phase Completion Criteria
- [ ] Java version successfully upgraded to 17
- [ ] Spring Boot upgraded to 3.x
- [ ] All javax → jakarta migrations complete
- [ ] All tests pass (100% success rate)  
- [ ] Coverage ≥80% maintained
- [ ] API contracts byte-for-byte identical to Java 11
- [ ] Performance within 5% of baseline
- [ ] No client-breaking changes

## Troubleshooting
See `spec/java22_upgrade/common/troubleshooting.md` for:
- javax → jakarta migration issues
- Spring Boot 3.x compatibility problems  
- Java 17 compilation errors
- Dependency version conflicts

## Next Phase
Upon completion, move to Phase 3: Java 17 → Java 21 Migration
