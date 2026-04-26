# Quickstart: Personal Blog Notes

## Prerequisites

- Java 25 installed and available on `PATH`
- Maven installed and available on `PATH`
- MySQL running locally or in a reachable environment
- A database created for this feature, for example `my_blog`

## Environment Setup

1. Create a MySQL database and an application user with read/write access.
2. Configure backend datasource, session, and mapper settings in
   `backend/src/main/resources/application.yml`.
3. Seed at least two user accounts so both author and reader flows can be
   verified.
4. Ensure `JAVA_HOME` points to a Java 25 installation before running Maven.

## Suggested Build Layout

1. Create the Maven backend project under `backend/`.
2. Place static frontend files under
   `backend/src/main/resources/static/`.
3. Organize browser assets into:
   - `css/` for shared styles
   - `js/` for API calls and page interactions
   - `pages/` for homepage, note detail, and login entry points

## TDD Workflow

1. Write a failing contract/controller test for the target story.
2. Write or update a failing service or mapper test for the same rule.
3. Implement the minimal code to make the tests pass.
4. Refactor while keeping the full test suite green.
5. Add or update a browser-flow regression test for the story's user journey.

## Local Commands

1. From `backend/`, run `mvn test` to execute the H2-backed contract, mapper,
   integration, and unit suites.
2. From `backend/`, run `mvn spring-boot:run` to start the application.
3. Open `http://localhost:8080/pages/login.html` after the server starts.

## Test Accounts

- Test fixtures use `alice`, `bob`, and `ally-reader`.
- The shared password in the automated test data is `password123`.

## Manual Validation Flow

### Story 1: Personal homepage and folders

1. Log in as an existing user.
2. Create a top-level folder.
3. Create a note inside that folder.
4. Confirm the homepage lists the note under the selected folder.

### Story 2: Markdown editing

1. Open a note you own.
2. Switch to edit mode.
3. Save Markdown containing headings, lists, code blocks, and an image URL.
4. Return to display mode and verify the rendered result.

### Story 3: Search and read-only browsing

1. Log in as a different user.
2. Search for another account using a partial string.
3. Open the target user's homepage.
4. Open one of that user's notes and confirm it is read-only.

## Minimum Test Suite Before Tasks

- Login success and failure tests
- Homepage loading tests for owner and non-owner views
- Folder creation validation tests
- Note creation/update validation tests
- Read-only permission enforcement tests
- User search result tests
- Markdown rendering and sanitization tests

## Plan Artifacts

- [plan.md](./plan.md)
- [research.md](./research.md)
- [data-model.md](./data-model.md)
- [blog-api.yaml](./contracts/blog-api.yaml)
