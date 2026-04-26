<!--
Sync Impact Report
Version change: template -> 1.0.0
Modified principles:
- Template Principle 1 -> I. Simplicity First
- Template Principle 2 -> II. Code Quality Is Product Quality
- Template Principle 3 -> III. Test-Driven Development (Non-Negotiable)
- Template Principle 4 -> IV. User Experience Before Raw Speed
- Template Principle 5 -> V. Small, Reviewable Changes
Added sections:
- Product Constraints
- Delivery Workflow
Removed sections:
- None
Templates requiring updates:
- ✅ .specify/templates/plan-template.md
- ✅ .specify/templates/spec-template.md
- ✅ .specify/templates/tasks-template.md
- N/A .specify/templates/commands/*.md (directory not present)
Follow-up TODOs:
- None
-->
# My Blog Constitution

## Core Principles

### I. Simplicity First
Every feature MUST start from the smallest implementation that solves a
reader-facing problem in the personal blog. New layers, dependencies,
abstractions, and configuration are forbidden unless they remove demonstrable
duplication or satisfy a committed near-term requirement. Rationale: a personal
blog gains reliability and maintainability from fewer moving parts, not
architectural ambition.

### II. Code Quality Is Product Quality
All production changes MUST leave the codebase easier to understand than before.
Code MUST favor clear naming, small modules, explicit boundaries, consistent
formatting, and automated checks where available. Reviews MUST reject dead code,
hidden coupling, and speculative extensibility. Rationale: in a small web
application, internal clarity directly determines release safety and long-term
delivery speed.

### III. Test-Driven Development (Non-Negotiable)
Tests MUST be written before implementation for every behavior change. The
required sequence is: define the acceptance or regression test, observe failure,
implement the minimal change, and refactor only with tests green. A task is not
complete until the relevant automated tests pass. Rationale: mandatory TDD keeps
quality from drifting and makes future changes safer.

### IV. User Experience Before Raw Speed
Decisions MUST optimize for clarity, stability, accessibility, and perceived
smoothness before raw response-time metrics. Intentional latency trade-offs are
allowed only when they improve reader experience, such as better content
rendering, clearer feedback, or reduced cognitive load, and they MUST include
visible loading, empty, and error states. Rationale: this product is judged by
how it feels to use, not by isolated benchmark wins.

### V. Small, Reviewable Changes
Work MUST be delivered in thin slices that can be understood, tested, and
reverted quickly. Each change MUST map to a user-visible outcome or a clearly
justified enabling step, and each plan or task list MUST keep stories
independently deliverable. Rationale: small increments reduce risk and make TDD,
review, and UX validation practical.

## Product Constraints

This repository governs a personal blog web application. The default approach
MUST be a simple, conventional web stack with minimal infrastructure.
Accessibility, readability, and content-first interaction patterns are
mandatory. Performance targets MUST be expressed in user-facing terms such as
stable rendering, understandable navigation, and clear feedback, not throughput
alone. Any increase in architectural complexity or intentional slowdown MUST be
explicitly justified in the implementation plan.

## Delivery Workflow

Every feature spec MUST define prioritized user scenarios, acceptance criteria,
explicit scope boundaries, and the tests that will be written first. Every
implementation plan MUST pass a constitution check covering simplicity, code
quality, TDD scope, and UX impact before design begins. Every task list MUST
place test tasks before implementation tasks and include validation for loading,
empty, error, and success states wherever user-facing behavior changes. Merges
require green automated tests and manual verification of changed user journeys.

## Governance

This constitution overrides local habit and temporary convenience. Amendments
MUST update this document and every affected template in the same change.
Versioning follows semantic versioning: MAJOR for incompatible governance
changes, MINOR for new or materially expanded principles or sections, and PATCH
for clarifications that do not change meaning. Compliance MUST be checked during
planning, task generation, code review, and pre-release verification.
Exceptions MUST be documented in the relevant plan under Complexity Tracking
with a concrete rationale and sunset condition.

**Version**: 1.0.0 | **Ratified**: 2026-04-24 | **Last Amended**: 2026-04-24
