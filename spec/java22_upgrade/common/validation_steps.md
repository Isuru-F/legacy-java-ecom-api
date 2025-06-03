# Shared Validation Steps

## Build Validation
```bash
# Standard build check - use before/after any changes
mvn clean compile
# Expected: BUILD SUCCESS

# Standard test check - use before/after any changes  
mvn test
# Expected: All tests pass, 0 failures

# Coverage report generation
mvn clean test jacoco:report
# Expected: Coverage report in target/site/jacoco/index.html
```

## Environment State Checks
```bash
# Check Java version
java -version
# Document current version

# Check Maven version
mvn -version
# Ensure compatible with project

# Check project structure
ls -la src/main/java/com/ecommerce/legacy/
# Verify expected packages present
```

## Coverage Validation
```bash
# Generate and check coverage
mvn clean test jacoco:report

# Extract coverage percentage (Linux/Mac)
grep -o "Total[^%]*%" target/site/jacoco/index.html | head -1

# Minimum thresholds:
# Overall: 80%
# Per package: 70%
```

## API Contract Validation
```bash
# Start application
mvn spring-boot:run &
APP_PID=$!
sleep 10

# Test all endpoints respond
curl -s http://localhost:8080/api/health
curl -s http://localhost:8080/api/users
curl -s http://localhost:8080/api/products

# Cleanup
kill $APP_PID
```

## Performance Baseline Capture
```bash
# Build time measurement
time mvn clean compile

# Test execution time
time mvn test

# Application startup time
time mvn spring-boot:run &
# Monitor logs for "Started Application in X seconds"
```

## Pre-Phase Validation Checklist
Before starting any phase:
- [ ] `mvn clean compile` succeeds
- [ ] `mvn test` passes 100%
- [ ] Coverage report generates
- [ ] Application starts successfully
- [ ] All APIs respond correctly

## Post-Step Validation Pattern
After each step:
1. Run build validation
2. Run test validation  
3. Update environment_state in progress.md
4. Document any issues in failures_and_retries
