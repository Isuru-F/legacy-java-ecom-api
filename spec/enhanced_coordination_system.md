# Enhanced Coordination System for Java 22 Upgrade

## The State Management Problem

The agent needs to know:
- **Exactly where** it left off (not just "Phase 1 in progress")
- **What was already validated** (avoid re-running expensive operations)
- **What failed and why** (don't repeat failed approaches)
- **Environmental state** (current Java version, test status, etc.)
- **Next specific action** to take

## Solution: Detailed State Tracking

### 1. Enhanced progress.md Structure
```yaml
upgrade_status:
  current_phase: "phase_1_baseline"
  current_step: "1.2.3"
  current_task: "Generate coverage report with JaCoCo"
  
  phase_completion:
    phase_0_coverage: "completed"
    phase_1_baseline: "in_progress"
    phase_2_java17: "pending"
    phase_3_java21: "pending"
    phase_4_java22: "pending"
  
  step_completion:
    "1.1": "completed"
    "1.2.1": "completed"
    "1.2.2": "completed"
    "1.2.3": "in_progress"
    
  environment_state:
    java_version: "11"
    tests_passing: true
    build_successful: true
    coverage_percentage: "75%"
    last_validation: "2025-01-03T10:30:00Z"
    
  failures_and_retries:
    "1.2.2":
      attempts: 1
      last_error: "JaCoCo plugin configuration issue"
      resolution: "Updated pom.xml with correct plugin version"
      
  next_action:
    description: "Execute mvn clean test jacoco:report to generate coverage"
    command: "mvn clean test jacoco:report"
    expected_result: "Coverage report in target/site/jacoco/"
    on_success: "Move to step 1.2.4"
    on_failure: "Check JaCoCo configuration in pom.xml"
```

### 2. Phase Files with Granular Steps

Each phase file includes:
- **Numbered steps** (1.1, 1.2.1, 1.2.2, etc.)
- **Validation commands** for each step
- **Failure recovery instructions**
- **Prerequisites check**
- **State transition rules**

Example phase_1_baseline.md:
```markdown
## Prerequisites Check
- [ ] Execute: `check_environment.sh` to validate Phase 0 completion
- [ ] Verify: progress.md shows phase_0_coverage: "completed"
- [ ] Validate: All tests passing with `mvn test`

## Step 1.1: Environment Validation
**Current Step ID**: 1.1
**Command**: `mvn clean compile && mvn test`
**Expected**: Build success + all tests pass
**On Success**: Update progress.md step "1.1": "completed", move to 1.2
**On Failure**: See troubleshooting.md section "Build Failures"

## Step 1.2: Coverage Enhancement
### Step 1.2.1: Add JaCoCo Plugin
**Current Step ID**: 1.2.1
**Action**: Add JaCoCo plugin to pom.xml
**Validation**: `mvn help:describe -Dplugin=jacoco`
**Expected**: Plugin recognized
**On Success**: Mark 1.2.1 complete, move to 1.2.2

### Step 1.2.2: Configure Coverage Goals
**Current Step ID**: 1.2.2
**Action**: Configure coverage reporting in pom.xml
**Validation**: Check pom.xml has jacoco:report goal
**On Success**: Mark 1.2.2 complete, move to 1.2.3
```

### 3. Coordination Script

Create `spec/java22_upgrade/coordinator.md`:
```markdown
## Agent Self-Coordination Instructions

### Startup Sequence
1. **Read State**: Load progress.md to get current_phase, current_step, next_action
2. **Validate Environment**: Run environment checks from current phase file
3. **Resume Execution**: Execute the specific next_action command
4. **Handle Result**: Follow on_success or on_failure path
5. **Update State**: Write new state to progress.md
6. **Continue**: Repeat until phase complete

### State Update Pattern
After each action:
```bash
# Update progress.md with new state
echo "step_completion['1.2.3']: 'completed'" >> progress.md
echo "current_step: '1.2.4'" >> progress.md
echo "last_validation: $(date -u +%Y-%m-%dT%H:%M:%SZ)" >> progress.md
```

### Error Handling
If action fails:
1. Check failures_and_retries for previous attempts
2. If first failure: Try resolution from phase file
3. If repeated failure: Document in progress.md, request human intervention
4. Always preserve state for resumption

### Phase Transition
When current_step reaches end of phase:
1. Run phase completion validation
2. Update phase_completion status
3. Set current_phase to next phase
4. Reset current_step to first step of new phase
5. Update next_action for new phase
```

### 4. Prompting Strategy

**Initial Prompt**:
```
"Continue the Java 22 upgrade project. Read spec/java22_upgrade/progress.md to determine current state, then load the current phase file and execute the next_action. Use todo_write to track progress within the current step."
```

**Resume Prompt**:
```
"Resume Java 22 upgrade from last checkpoint. Check progress.md for current_step and next_action, then continue execution. Report any blockers encountered."
```

### 5. Environment Validation Scripts

Create helper scripts the agent can run:
```bash
# check_environment.sh
#!/bin/bash
echo "Java version: $(java -version 2>&1 | head -1)"
echo "Maven version: $(mvn -version | head -1)"
echo "Build status: $(mvn clean compile >/dev/null 2>&1 && echo 'SUCCESS' || echo 'FAILED')"
echo "Test status: $(mvn test >/dev/null 2>&1 && echo 'PASSED' || echo 'FAILED')"
echo "Coverage available: $(test -f target/site/jacoco/index.html && echo 'YES' || echo 'NO')"
```

## Benefits of Enhanced System

1. **Precise Resumption**: Agent knows exactly which command to run next
2. **Failure Recovery**: Clear paths for handling common failures
3. **State Validation**: Environment checks ensure consistency
4. **Progress Tracking**: Detailed history of what was attempted
5. **Human Handoff**: Clear documentation when manual intervention needed

## Implementation Plan

1. Create the enhanced progress.md structure
2. Split phases into granular, numbered steps
3. Add validation commands and failure paths
4. Create environment checking scripts
5. Test with Phase 0 to validate approach

Would you like me to implement this enhanced coordination system?
