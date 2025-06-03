# How to Start the Java 22 Upgrade

## Exact Prompt to Use

Copy and paste this exact prompt to start the Java 22 upgrade work:

```
Start the Java 22 upgrade project. Read spec/java22_upgrade/progress.md to determine the current phase and step, then load the current phase file and execute the next_action. Use todo_write to track progress within the current step. Follow the coordination instructions in spec/java22_upgrade/coordinator.md for state management.
```

## What the Agent Will Do

1. **Read Current State**: Load `spec/java22_upgrade/progress.md` to see current phase and next action
2. **Load Phase File**: Load the current phase file (starts with `spec/java22_upgrade/phases/phase_0_coverage.md`)
3. **Load Common Files**: Load validation steps and troubleshooting as needed
4. **Execute Next Action**: Run the specific command from `next_action.command`
5. **Update State**: Update progress.md with results and new next_action
6. **Continue**: Move through steps systematically until phase complete

## File Structure Created

```
spec/java22_upgrade/
├── README.md                    # Project overview
├── progress.md                  # Current state and next actions  
├── coordinator.md               # Agent execution instructions
├── common/
│   ├── validation_steps.md      # Shared validation commands
│   ├── troubleshooting.md       # Common issues and solutions
│   └── commit_templates.md      # Git commit message templates
└── phases/
    ├── phase_0_coverage.md      # Test coverage assessment (80%+ required)
    ├── phase_1_baseline.md      # API baseline and performance metrics
    ├── phase_2_java17.md        # Java 11→17 + Spring Boot 2→3 migration
    ├── phase_3_java21.md        # Java 17→21 migration with optimizations
    └── phase_4_java22.md        # Java 21→22 final migration
```

## Current State

The agent will start with:
- **Current Phase**: phase_0_coverage
- **Current Step**: 0.1.1 (Install JaCoCo Maven Plugin)
- **Next Action**: Check current build and test status with `mvn clean compile && mvn test`

## Context Efficiency

Each execution loads only ~500 lines total:
- Current phase file: ~250 lines
- Validation steps: ~100 lines  
- Progress/coordinator: ~150 lines

vs. the original 1049-line monolithic spec file.

## Agent Resumability

The agent can resume from any point because:
- Exact current step and next command stored in progress.md
- Environment state tracked (Java version, test status, etc.)
- Failure recovery paths documented
- Cross-references between files for context

## Human Handoff

If blockers occur:
- All attempts and failures documented in progress.md
- Clear escalation criteria in troubleshooting.md
- Specific step and context preserved for resumption
- Human can easily see exactly where agent stopped

The system is now ready for autonomous execution!
