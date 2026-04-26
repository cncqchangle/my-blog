# Implementation Plan: Personal Blog Notes

**Branch**: `001-web-markdown` | **Date**: 2026-04-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-blog-note-sharing/spec.md`

**Note**: This plan turns the approved feature spec into research, design, and
implementation-ready artifacts.

## Summary

Build a personal blog web application where authenticated users manage notes on
their own homepage, organize every note into exactly one top-level folder, edit
Markdown notes in author-only mode, and search for other users to browse their
published notes in read-only mode. The solution will use a single Spring Boot
application on Java 25 to serve both JSON APIs and static HTML/CSS/JavaScript
pages, with MyBatis for persistence and MySQL as the system of record, while
maintaining keyboard-accessible core flows and explicit feedback states.

## Technical Context

**Language/Version**: Java 25, HTML5, CSS3, vanilla JavaScript (ES2023)
**Primary Dependencies**: Spring Boot 4.0.6, Spring Web, Spring Security,
MyBatis Spring Boot Starter, MySQL Driver, CommonMark, HTML sanitizer library
**Storage**: MySQL for users, folders, notes, and session-related persistence;
image references stored inside Markdown content as external URLs
**Testing**: JUnit 5, Spring Boot Test, MockMvc, MyBatis integration tests,
repository SQL tests, browser-flow regression checks for critical journeys
**Target Platform**: Modern desktop and mobile browsers; Linux-based server
deployment
**Project Type**: Monolithic web application with server-hosted static frontend
**Performance Goals**: Homepage, search, and note-detail views show a clear
loading or content state within 1 second for normal usage; note save confirms
success or failure immediately after response
**Constraints**: Mandatory TDD, every note belongs to exactly one folder,
non-authors are read-only, architecture must stay simple, frontend must use
HTML + CSS + JavaScript, backend must use Java 25 + Spring Boot 4.0.6 +
Maven + MyBatis + MySQL, and core flows must support keyboard navigation with
visible focus and semantic status feedback
**Scale/Scope**: Single-application personal blog for authenticated authors and
readers; hundreds of users, thousands of notes, and a modest number of
concurrent sessions

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Initial Gate Review**

- **Simplicity First**: Pass. The design stays within a single Spring Boot
  deployable that serves static pages and JSON APIs. Image insertion uses
  Markdown image references instead of a separate media subsystem.
- **Code Quality Is Product Quality**: Pass. The plan requires layered package
  boundaries, mapper-level SQL tests, controller/service regression coverage,
  and explicit permission checks on every write path.
- **Test-Driven Development**: Pass. Each story will begin with failing tests
  for homepage browsing, note editing, search, and read-only enforcement before
  implementation starts.
- **User Experience Before Raw Speed**: Pass. All user-facing flows include
  loading, empty, error, and success states, and the plan includes an
  accessibility baseline for keyboard navigation and semantic UI feedback.
- **Small, Reviewable Changes**: Pass. The implementation can be delivered in
  three slices: personal homepage and folders, author editing, then public
  search and read-only browsing, with shared read-only note querying extracted
  into foundational work.

**Post-Design Review**

- **Simplicity First**: Pass. The data model uses four focused entities and the
  contract surface is limited to the endpoints required by the spec.
- **Code Quality Is Product Quality**: Pass. Research decisions standardize
  Markdown rendering, sanitization, and session handling to avoid ad hoc code.
- **Test-Driven Development**: Pass. Quickstart and research artifacts define
  controller, service, mapper, and user-journey-first testing expectations.
- **User Experience Before Raw Speed**: Pass. Contracts include explicit UX
  states, timing verification tasks, and accessibility validation for core
  flows.
- **Small, Reviewable Changes**: Pass. Contracts and data model preserve
  separable implementation slices without speculative features, and reader
  browsing no longer depends on author editing.

## Project Structure

### Documentation (this feature)

```text
specs/001-blog-note-sharing/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- blog-api.yaml
`-- spec.md
```

### Source Code (repository root)

```text
backend/
|-- pom.xml
`-- src/
    |-- main/
    |   |-- java/com/example/myblog/
    |   |   |-- config/
    |   |   |-- controller/
    |   |   |-- domain/
    |   |   |-- mapper/
    |   |   |-- service/
    |   |   `-- security/
    |   `-- resources/
    |       |-- mapper/
    |       |-- application.yml
    |       `-- static/
    |           |-- css/
    |           |-- js/
    |           `-- pages/
    `-- test/
        `-- java/com/example/myblog/
            |-- contract/
            |-- integration/
            |-- mapper/
            `-- unit/
```

**Structure Decision**: Use one backend module that contains the Spring Boot
runtime and serves static frontend assets from `src/main/resources/static`.
This keeps the deployment unit simple while preserving a clean boundary between
browser assets and Java application code.

## Complexity Tracking

No constitution violations are currently identified. The only non-trivial
dependency additions are:

| Decision | Why Needed | Simpler Alternative Rejected Because |
|----------|------------|--------------------------------------|
| Spring Security for session auth | Prevents custom authentication and authorization defects | Hand-rolled session checks would increase security risk and maintenance cost |
| CommonMark + HTML sanitization | Ensures Markdown display is consistent and safe | Rendering raw Markdown or trusting HTML would create broken UX or XSS risk |
