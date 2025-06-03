# Phase 1: Pre-Migration Setup and Baseline Testing

## Objectives
- Establish comprehensive API testing framework
- Document current API contracts and behavior
- Create baseline performance metrics
- Set up client compatibility validation

## Prerequisites
- Phase 0 successfully completed
- 80%+ test coverage achieved
- All tests passing
- Build completing without errors

## Steps

### Step 1.1: Enhanced Test Coverage Validation

#### Step 1.1.1: Verify Phase 0 Completion
**Step ID**: 1.1.1
**Description**: Validate Phase 0 criteria are still met
**Commands**:
```bash
mvn clean compile
mvn test
mvn jacoco:report
```
**Expected Result**: Build success, tests pass, coverage ≥80%
**Validation**: Check progress.md shows phase_0_coverage: "completed"
**On Success**: Move to step 1.1.2
**On Failure**: Return to Phase 0 to fix issues

#### Step 1.1.2: API Endpoint Discovery
**Step ID**: 1.1.2
**Description**: Document all REST endpoints in the application
**Commands**:
```bash
# Find all controller classes
find src/main/java -name "*Controller.java" -exec grep -l "@RestController\|@Controller" {} \;

# Extract endpoint mappings
grep -r "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" src/main/java/
```
**Expected Result**: Complete list of all API endpoints
**Validation**: Document endpoints in progress.md environment_state
**On Success**: Move to step 1.2.1
**On Failure**: Investigate controller scanning issues

### Step 1.2: API Contract Testing

#### Step 1.2.1: Create Integration Test Framework
**Step ID**: 1.2.1
**Description**: Set up comprehensive integration tests for all endpoints
**Commands**:
```bash
# Check existing integration tests
find src/test -name "*IntegrationTest.java" -o -name "*IT.java"

# Verify Spring Boot Test configuration
grep -r "@SpringBootTest" src/test/
```
**Action**: Create integration tests for each discovered endpoint
**Expected Result**: Integration test class for each controller
**Validation**: Each endpoint has GET/POST/PUT/DELETE tests as applicable
**On Success**: Move to step 1.2.2
**On Failure**: Add missing integration test structure

#### Step 1.2.2: API Response Contract Validation
**Step ID**: 1.2.2
**Description**: Capture and validate API response formats
**Commands**:
```bash
# Start application for testing
mvn spring-boot:run &
APP_PID=$!
sleep 15

# Test sample endpoints
curl -s http://localhost:8080/api/users | jq .
curl -s http://localhost:8080/api/products | jq .

# Stop application
kill $APP_PID
```
**Expected Result**: All endpoints return valid JSON responses
**Validation**: Response formats documented for comparison
**On Success**: Move to step 1.3.1
**On Failure**: Fix API endpoint issues

### Step 1.3: Performance Baseline Establishment

#### Step 1.3.1: Application Startup Time Baseline
**Step ID**: 1.3.1
**Description**: Measure and document application startup performance
**Commands**:
```bash
# Measure startup time
time mvn spring-boot:run &
APP_PID=$!

# Monitor logs for startup completion
tail -f target/spring-boot.log | grep -m 1 "Started.*Application"

# Stop application
kill $APP_PID
```
**Expected Result**: Startup time measured and documented
**Validation**: Startup time recorded in progress.md
**On Success**: Move to step 1.3.2
**On Failure**: Investigate startup performance issues

#### Step 1.3.2: API Response Time Baseline
**Step ID**: 1.3.2
**Description**: Measure response times for all endpoints
**Commands**:
```bash
# Start application
mvn spring-boot:run &
APP_PID=$!
sleep 15

# Measure response times for each endpoint
curl -w "@curl-format.txt" -s http://localhost:8080/api/users
curl -w "@curl-format.txt" -s http://localhost:8080/api/products

# Stop application
kill $APP_PID
```
**Action**: Create curl-format.txt for timing measurements
**Expected Result**: Response time baseline for all endpoints
**Validation**: Times recorded in progress.md environment_state
**On Success**: Move to step 1.3.3
**On Failure**: Check application performance issues

#### Step 1.3.3: Memory Usage Baseline
**Step ID**: 1.3.3
**Description**: Capture memory usage patterns
**Commands**:
```bash
# Start application with JVM monitoring
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m -XX:+PrintGCDetails" &
APP_PID=$!
sleep 15

# Monitor memory usage
jps | grep Application
jstat -gc $APP_PID

# Load test endpoints
for i in {1..10}; do
  curl -s http://localhost:8080/api/users > /dev/null
  curl -s http://localhost:8080/api/products > /dev/null
done

# Final memory check
jstat -gc $APP_PID

# Stop application  
kill $APP_PID
```
**Expected Result**: Memory usage baseline established
**Validation**: Memory metrics documented
**On Success**: Move to step 1.4.1
**On Failure**: Investigate memory issues

### Step 1.4: Client Compatibility Framework

#### Step 1.4.1: Create Client Simulation Tests
**Step ID**: 1.4.1
**Description**: Create tests that simulate client application usage
**Commands**:
```bash
# Create client simulation test class
find src/test -name "*ClientSimulationTest.java"
```
**Action**: Create comprehensive client simulation tests
**Expected Result**: Test class simulating real client interactions
**Validation**: Client simulation tests pass
**On Success**: Move to step 1.4.2
**On Failure**: Debug client simulation setup

#### Step 1.4.2: Regression Detection Setup
**Step ID**: 1.4.2
**Description**: Set up automated regression detection
**Commands**:
```bash
# Create baseline comparison utilities
ls src/test/java/*/util/*Baseline*
```
**Action**: Create utilities for comparing API responses and performance
**Expected Result**: Regression detection framework operational
**Validation**: Baseline comparison tests run successfully
**On Success**: Move to step 1.5.1
**On Failure**: Fix baseline comparison logic

### Step 1.5: Final Phase Validation

#### Step 1.5.1: Complete Integration Test Run
**Step ID**: 1.5.1
**Description**: Execute complete test suite including new integration tests
**Commands**:
```bash
mvn clean test
mvn jacoco:report
```
**Expected Result**: All tests pass, coverage maintained
**Validation**: 
- 100% test pass rate
- Coverage ≥80% maintained
- No regression in test execution time
**On Success**: Move to step 1.5.2
**On Failure**: Fix failing tests

#### Step 1.5.2: Performance Regression Validation
**Step ID**: 1.5.2
**Description**: Validate no performance regression from baseline setup
**Commands**:
```bash
# Re-run performance measurements
time mvn clean compile
time mvn test
```
**Expected Result**: Performance within 5% of original baseline
**Validation**: Build and test times compared to Phase 0 baseline
**On Success**: Phase 1 complete, update progress.md to phase_2_java17
**On Failure**: Investigate performance regression

## Phase Completion Criteria
- [ ] All API endpoints documented and tested
- [ ] Integration tests cover all controller endpoints
- [ ] Performance baseline established for all metrics
- [ ] Client simulation framework operational
- [ ] All tests pass (100% success rate)
- [ ] Coverage maintained at 80%+
- [ ] No performance regression detected

## Troubleshooting
See `spec/java22_upgrade/common/troubleshooting.md` for:
- Integration test setup issues
- Application startup problems
- Performance measurement failures
- API endpoint testing problems

## Next Phase
Upon completion, move to Phase 2: Java 11 → Java 17 Migration
