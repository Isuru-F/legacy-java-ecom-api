# Modular Java 22 Upgrade Specification Structure

## Problem with Current Approach
- Single 1049-line file wastes context window
- Phases include unnecessary information from other phases
- Repetitive validation steps and templates
- Hard to maintain and update individual phases

## Proposed Modular Structure

```
spec/
├── java22_upgrade/
│   ├── README.md                    # Project overview and current status
│   ├── common/
│   │   ├── validation_steps.md      # Shared validation procedures
│   │   ├── commit_templates.md      # Git commit message templates
│   │   ├── success_criteria.md      # Shared success criteria
│   │   └── troubleshooting.md       # Common blockers and solutions
│   ├── phases/
│   │   ├── phase_0_coverage.md      # Test coverage assessment
│   │   ├── phase_1_baseline.md      # Baseline testing setup
│   │   ├── phase_2_java17.md        # Java 11 → 17 upgrade
│   │   ├── phase_3_java21.md        # Java 17 → 21 upgrade
│   │   └── phase_4_java22.md        # Java 21 → 22 upgrade
│   ├── progress.md                  # Overall progress tracking
│   └── coordination.md              # How to execute phases recursively
```

## Individual Phase File Structure

Each phase file (~200-300 lines max) contains:
1. **Prerequisites** - What must be complete before starting
2. **Objectives** - What this phase accomplishes
3. **Tasks** - Specific steps for this phase only
4. **Validation** - Phase-specific validation steps
5. **Success Criteria** - When this phase is complete
6. **Next Phase** - What comes after this phase

## Recursive Execution Strategy

### 1. Coordination Script Approach
Create `spec/java22_upgrade/coordination.md` with prompting strategy:

```markdown
## Agent Execution Instructions

### Current Phase Detection
1. Read `progress.md` to determine current phase
2. Load only the current phase file + common files
3. Execute current phase completely
4. Update progress and move to next phase

### Context Management
- Load max 3 files per execution: current phase + 2 common files
- Use todo_write to track within-phase progress
- Update progress.md after each major milestone
```

### 2. Phase Hand-off Pattern
Each phase ends with:
- Update progress.md with completion status
- Specify next phase to load
- Clear todos for current phase
- Set up todos for next phase

### 3. Self-Coordinating Prompts
```
"Execute the next phase of the Java 22 upgrade. Read progress.md to determine current phase, then load only the current phase specification and execute it completely."
```

## Benefits of This Structure

1. **Context Efficiency**: Each phase file ~250 lines vs 1049 lines
2. **Maintainability**: Update phases independently
3. **Reusability**: Common files shared across phases
4. **Clarity**: Each phase has focused, specific objectives
5. **Resumability**: Can restart from any phase easily
6. **Progress Tracking**: Clear status at any point

## Implementation Strategy

1. **Extract Common Elements**: Move shared validation steps, templates, and procedures to common files
2. **Split Phases**: Create focused phase files with only relevant information
3. **Create Coordination**: Build the recursive execution framework
4. **Test Approach**: Validate with Phase 0 before implementing all phases

Would you like me to implement this modular structure?
