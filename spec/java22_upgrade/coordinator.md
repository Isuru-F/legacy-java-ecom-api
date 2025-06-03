# Agent Coordination Instructions

## Startup Sequence
When prompted to work on Java 22 upgrade:

1. **Read Current State**
   ```bash
   # Load these files to understand current state:
   # - spec/java22_upgrade/progress.md (current phase, step, next_action)
   # - spec/java22_upgrade/phases/{current_phase}.md (current phase details)
   # - spec/java22_upgrade/common/validation_steps.md (validation commands)
   ```

2. **Validate Environment** 
   - Check if project builds: `mvn clean compile`
   - Check if tests pass: `mvn test`
   - Document current state in progress.md

3. **Execute Next Action**
   - Run the command specified in progress.md next_action.command
   - Follow on_success or on_failure path based on result
   - Update step_completion and environment_state

4. **Update State and Continue**
   - Update progress.md with new current_step and next_action
   - Use todo_write to track within-step progress
   - Continue until phase complete or blocker encountered

## State Update Pattern
After each successful step:
```yaml
# Update these fields in progress.md:
step_completion:
  "{step_id}": "completed"
current_step: "{next_step_id}"
environment_state:
  last_validation: "{current_timestamp}"
next_action:
  description: "{next_task_description}"
  command: "{next_command_to_run}"
```

## Error Handling Protocol
1. **Document failure** in progress.md failures_and_retries
2. **Check troubleshooting** in common/troubleshooting.md
3. **Attempt resolution** if clear fix available
4. **Request human intervention** if complex blocker

## Phase Transition Protocol
When current phase reaches completion:
1. **Validate all phase requirements** met
2. **Update phase_completion** status to "completed"
3. **Set current_phase** to next phase
4. **Reset current_step** to first step of new phase
5. **Load new phase file** and set first next_action

## File Cross-References
- **Current Phase File**: `spec/java22_upgrade/phases/{current_phase}.md`
- **Validation Steps**: `spec/java22_upgrade/common/validation_steps.md`
- **Troubleshooting**: `spec/java22_upgrade/common/troubleshooting.md`
- **Templates**: `spec/java22_upgrade/common/commit_templates.md`

## Critical Rules
- **NEVER skip validation steps** - each step must be verified before moving forward
- **ALWAYS update progress.md** after completing any step
- **USE todo_write** to track progress within current step
- **LOAD minimal files** - only current phase + relevant common files
- **DOCUMENT blockers** clearly for human handoff if needed
