# Java 22 Upgrade Project

## Overview
Phased upgrade from Java 11 to Java 22 with zero API breaking changes and full backward compatibility.

## Current Status
Check `progress.md` for current phase and step status.

## Quick Start for Agent
```bash
# Agent should run this command to continue work:
# Read progress.md to determine current state, then load current phase file and execute next_action
```

## File Structure
- `progress.md` - Current state and next actions
- `coordinator.md` - Agent execution instructions
- `common/` - Shared validation steps, templates, troubleshooting
- `phases/` - Individual phase specifications

## Phases Overview
- **Phase 0**: Test coverage assessment and enhancement (80%+ required)
- **Phase 1**: Baseline testing framework and API contract validation  
- **Phase 2**: Java 11 → 17 + Spring Boot 2.x → 3.x migration
- **Phase 3**: Java 17 → 21 LTS with performance optimizations
- **Phase 4**: Java 21 → 22 with modern features

## Success Criteria
- Zero API breaking changes
- 80%+ test coverage maintained throughout
- Performance within 5% of baseline
- All tests passing at each phase

## Agent Instructions
To start or resume work: Load `coordinator.md` and follow the startup sequence.
