# Phase 0: Test Coverage Assessment and Enhancement

## Objectives
- Assess current test coverage state
- Install and configure JaCoCo coverage reporting
- Achieve minimum 80% overall coverage, 70% per package
- Establish coverage baseline and reporting infrastructure

## Prerequisites
- Project must be on Java 11
- All existing tests must pass
- Maven build must complete successfully

## Steps

### Step 0.1: Coverage Tools Installation and Setup

#### Step 0.1.1: Install JaCoCo Maven Plugin
**Step ID**: 0.1.1
**Description**: Add JaCoCo plugin to pom.xml for coverage reporting
**Commands**:
```bash
# Check if JaCoCo plugin already exists
grep -A 10 "jacoco-maven-plugin" pom.xml
```
**Action**: If not found, add JaCoCo plugin configuration to pom.xml
**Expected Result**: JaCoCo plugin configured in build plugins
**Validation**: `mvn help:describe -Dplugin=jacoco` should recognize plugin
**On Success**: Move to step 0.1.2
**On Failure**: Check pom.xml syntax and plugin configuration

#### Step 0.1.2: Verify Build and Test Infrastructure  
**Step ID**: 0.1.2
**Description**: Ensure project builds and tests run successfully
**Commands**:
```bash
mvn clean compile
mvn test
```
**Expected Result**: Build SUCCESS, all tests pass
**Validation**: Check exit codes and output for errors
**On Success**: Move to step 0.2.1
**On Failure**: Fix build/test issues using troubleshooting.md

### Step 0.2: Initial Coverage Assessment

#### Step 0.2.1: Generate Coverage Report
**Step ID**: 0.2.1  
**Description**: Execute JaCoCo to generate initial coverage report
**Commands**:
```bash
mvn clean test jacoco:report
```
**Expected Result**: Coverage report generated in target/site/jacoco/
**Validation**: Check target/site/jacoco/index.html exists and opens
**On Success**: Move to step 0.2.2
**On Failure**: Check JaCoCo configuration, see troubleshooting.md

#### Step 0.2.2: Coverage Analysis
**Step ID**: 0.2.2
**Description**: Analyze current coverage and identify gaps
**Commands**:
```bash
# Extract overall coverage percentage
grep -o "Total[^%]*%" target/site/jacoco/index.html | head -1

# List packages with low coverage
find target/site/jacoco -name "index.html" -exec grep -l "0%" {} \;
```
**Expected Result**: Coverage metrics documented
**Validation**: Coverage data recorded in progress.md
**On Success**: If coverage ≥80%, move to step 0.4.1; else move to step 0.3.1
**On Failure**: Re-run coverage generation

### Step 0.3: Coverage Enhancement (If Required)

#### Step 0.3.1: Controller Layer Testing
**Step ID**: 0.3.1
**Description**: Add integration tests for REST endpoints
**Commands**:
```bash
# Check existing controller tests
find src/test -name "*ControllerTest.java" -exec wc -l {} \;
```
**Action**: Add missing controller tests for all endpoints
**Expected Result**: All controllers have comprehensive integration tests
**Validation**: Re-run coverage to verify controller package ≥70%
**On Success**: Move to step 0.3.2
**On Failure**: Add more test scenarios

#### Step 0.3.2: Service Layer Testing
**Step ID**: 0.3.2
**Description**: Add unit tests for business logic methods
**Commands**:
```bash
# Check existing service tests
find src/test -name "*ServiceTest.java" -exec wc -l {} \;
```
**Action**: Add missing service layer unit tests
**Expected Result**: All services have comprehensive unit tests
**Validation**: Re-run coverage to verify service package ≥70%
**On Success**: Move to step 0.3.3
**On Failure**: Add more test scenarios

#### Step 0.3.3: Repository Layer Testing
**Step ID**: 0.3.3
**Description**: Add integration tests with H2 database
**Commands**:
```bash
# Check existing repository tests
find src/test -name "*RepositoryTest.java" -exec wc -l {} \;
```
**Action**: Add missing repository integration tests
**Expected Result**: All repositories have CRUD operation tests
**Validation**: Re-run coverage to verify repository package ≥70%
**On Success**: Move to step 0.3.4
**On Failure**: Add more test scenarios

#### Step 0.3.4: Model/Entity Testing
**Step ID**: 0.3.4
**Description**: Add tests for entity validation and relationships
**Commands**:
```bash
# Check existing model tests
find src/test -name "*Test.java" | grep -i model
```
**Action**: Add missing entity and model tests
**Expected Result**: All models have validation and relationship tests
**Validation**: Re-run coverage to verify model package ≥70%
**On Success**: Move to step 0.4.1
**On Failure**: Add more test scenarios

### Step 0.4: Coverage Validation and Reporting

#### Step 0.4.1: Generate Final Coverage Report
**Step ID**: 0.4.1
**Description**: Generate final coverage report and validate thresholds
**Commands**:
```bash
mvn clean test jacoco:report
```
**Expected Result**: Updated coverage report with enhanced coverage
**Validation**: Overall coverage ≥80%, all packages ≥70%
**On Success**: Move to step 0.4.2
**On Failure**: Return to step 0.3.1 to add more tests

#### Step 0.4.2: Quality Gates Validation
**Step ID**: 0.4.2
**Description**: Validate all quality gates before phase completion
**Commands**:
```bash
mvn clean compile  # Must succeed
mvn test          # Must pass 100%
mvn jacoco:report # Must generate report
```
**Expected Result**: All quality gates pass
**Validation**: 
- Build successful
- Tests 100% pass rate
- Coverage ≥80% overall, ≥70% per package
**On Success**: Phase 0 complete, update progress.md to phase_1_baseline
**On Failure**: Address specific failing quality gate

## Phase Completion Criteria
- [ ] All tests pass (100% success rate)
- [ ] Overall coverage ≥80%
- [ ] All packages ≥70% coverage  
- [ ] Build completes successfully
- [ ] Coverage reporting pipeline operational
- [ ] Performance baseline documented

## Troubleshooting
See `spec/java22_upgrade/common/troubleshooting.md` for common issues:
- JaCoCo plugin configuration problems
- Test failures and resolution
- Coverage threshold failures
- Build issues

## Next Phase
Upon completion, move to Phase 1: Baseline Testing Framework
