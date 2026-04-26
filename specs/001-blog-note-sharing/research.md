# Research: Personal Blog Notes

## Decision 1: Use a single Spring Boot application to host both APIs and static pages

- **Decision**: Keep the frontend as plain HTML, CSS, and JavaScript served from
  Spring Boot static resources, while exposing JSON APIs from the same
  application.
- **Rationale**: This is the simplest architecture that still satisfies the
  required frontend/backend stack. It avoids an extra frontend build pipeline,
  removes cross-origin concerns, and keeps deployment to one Maven-built
  artifact.
- **Alternatives considered**:
  - Separate frontend and backend deployments: rejected because it adds build,
    routing, and deployment complexity without solving a current problem.
  - Server-side templating only: rejected because the feature explicitly calls
    for HTML + CSS + JavaScript frontend behavior, including interactive edit
    and search flows.

## Decision 2: Use session-cookie authentication with Spring Security

- **Decision**: Authenticate users with username/password, store passwords as
  secure hashes, and maintain login state through server-managed HTTP sessions.
- **Rationale**: Session auth is the simplest secure fit for a server-rendered
  personal blog. It cleanly supports "my homepage" and permission checks for
  author-only editing without introducing token lifecycle complexity.
- **Alternatives considered**:
  - JWT-based authentication: rejected because it adds token management and
    invalidation complexity with no product requirement for stateless APIs.
  - Custom session handling without Spring Security: rejected because it would
    weaken security and duplicate proven framework behavior.

## Decision 3: Store Markdown source and render sanitized HTML on the backend

- **Decision**: Persist each note's Markdown source and generate sanitized HTML
  when returning note detail responses and save results.
- **Rationale**: Backend rendering guarantees consistent output for both author
  preview and reader display, while sanitization protects note viewers from
  unsafe content embedded in Markdown.
- **Alternatives considered**:
  - Render Markdown only in the browser: rejected because readers and authors
    could see inconsistent results and security handling would be fragmented.
  - Store only rendered HTML: rejected because it would make editing lossy and
    break the Markdown-first authoring requirement.

## Decision 4: Treat inserted images as Markdown image references, not uploaded binaries

- **Decision**: V1 image support will use Markdown image syntax referencing image
  URLs. The system stores and renders these references but does not introduce a
  dedicated upload/media subsystem in this feature.
- **Rationale**: This satisfies the user requirement that notes can include
  images while keeping the data model and infrastructure minimal. It aligns with
  the constitution's simplicity rule.
- **Alternatives considered**:
  - Built-in file uploads and media storage: rejected for now because it
    introduces storage, validation, and lifecycle complexity beyond the current
    note-sharing scope.
  - No image support: rejected because it conflicts with the approved spec.

## Decision 5: Model search as case-insensitive fuzzy account lookup backed by MySQL

- **Decision**: Implement user search with case-insensitive partial matching on
  the unique account string and return a capped list of matches ordered by
  closeness and stable account ordering.
- **Rationale**: This directly matches the product requirement and can be served
  efficiently with straightforward SQL and sensible limits.
- **Alternatives considered**:
  - Exact-match only search: rejected because it does not satisfy fuzzy search.
  - Dedicated full-text search engine: rejected because it adds unjustified
    infrastructure for a small user/account search domain.

## Decision 6: Keep the domain model small and explicit

- **Decision**: Use four primary entities: `UserAccount`, `Folder`, `Note`, and
  `UserSessionView` at the API boundary. Persist only the first three in MySQL.
- **Rationale**: The spec centers on users, top-level folders, and notes. All
  other concerns, including rendered content and search results, can be modeled
  as derived views rather than permanent tables.
- **Alternatives considered**:
  - Separate publication or permission tables: rejected because permissions are
    determined directly by note ownership in the approved scope.
  - Nested folder trees: rejected because the spec explicitly limits folders to
    one level.

## Decision 7: Enforce TDD across mapper, service, controller, and browser flows

- **Decision**: Implement each story by writing failing tests in this order:
  contract/controller tests for API behavior, service tests for business rules,
  mapper tests for SQL-backed constraints, and browser-flow regression tests for
  critical user journeys.
- **Rationale**: This sequence supports the constitution's non-negotiable TDD
  rule and keeps bugs close to the layer where they originate.
- **Alternatives considered**:
  - UI-only testing: rejected because permission and persistence defects would
    be harder to isolate.
  - Unit tests only: rejected because the feature depends on SQL mapping,
    session auth, and end-to-end permissions.
