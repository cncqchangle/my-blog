# Tasks: Personal Blog Notes

**Input**: Design documents from `/specs/001-blog-note-sharing/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are MANDATORY. For each user story, create the failing
automated tests before implementation tasks and keep them in the task list.

**Organization**: Tasks are grouped by user story to enable independent
implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Web app**: `backend/src/main/`, `backend/src/test/`
- Browser assets are served from `backend/src/main/resources/static/`
- MyBatis XML mappings live in `backend/src/main/resources/mapper/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and base layout for the monolithic Spring Boot application

- [X] T001 Create the Maven Spring Boot project descriptor in `backend/pom.xml`
- [X] T002 Create the Spring Boot application entrypoint in `backend/src/main/java/com/example/myblog/MyBlogApplication.java` and align the package layout under `backend/src/main/java/com/example/myblog/`
- [X] T003 [P] Configure base application properties for MySQL, sessions, MyBatis, and test profiles in `backend/src/main/resources/application.yml`
- [X] T004 [P] Create initial static frontend entry files in `backend/src/main/resources/static/pages/login.html`, `backend/src/main/resources/static/pages/home.html`, and `backend/src/main/resources/static/pages/note.html`
- [X] T005 [P] Create the shared frontend asset shell in `backend/src/main/resources/static/css/app.css`, `backend/src/main/resources/static/js/api.js`, and `backend/src/main/resources/static/js/auth.js`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

- [X] T006 Create the MySQL schema for `user_account`, `folder`, and `note` in `backend/src/main/resources/schema.sql`
- [X] T007 [P] Implement shared domain entities in `backend/src/main/java/com/example/myblog/domain/UserAccount.java`, `backend/src/main/java/com/example/myblog/domain/Folder.java`, and `backend/src/main/java/com/example/myblog/domain/Note.java`
- [X] T008 [P] Create MyBatis mapper interfaces in `backend/src/main/java/com/example/myblog/mapper/UserAccountMapper.java`, `backend/src/main/java/com/example/myblog/mapper/FolderMapper.java`, and `backend/src/main/java/com/example/myblog/mapper/NoteMapper.java`
- [X] T009 [P] Implement MyBatis SQL mappings in `backend/src/main/resources/mapper/UserAccountMapper.xml`, `backend/src/main/resources/mapper/FolderMapper.xml`, and `backend/src/main/resources/mapper/NoteMapper.xml`
- [X] T010 [P] Create the login success and failure contract tests for `POST /api/auth/login` and `POST /api/auth/logout` in `backend/src/test/java/com/example/myblog/contract/AuthControllerTest.java`
- [X] T011 [P] Create the session authentication integration test in `backend/src/test/java/com/example/myblog/integration/AuthFlowTest.java`
- [X] T012 [P] Create the password lookup and account uniqueness mapper tests in `backend/src/test/java/com/example/myblog/mapper/UserAccountMapperTest.java`
- [X] T013 Implement Spring Security session authentication and password hashing in `backend/src/main/java/com/example/myblog/config/SecurityConfig.java` and `backend/src/main/java/com/example/myblog/security/SessionUserDetailsService.java`
- [X] T014 Implement login and logout application services and controller in `backend/src/main/java/com/example/myblog/service/AuthService.java` and `backend/src/main/java/com/example/myblog/controller/AuthController.java`
- [X] T015 Implement shared API error handling and validation responses in `backend/src/main/java/com/example/myblog/controller/ApiExceptionHandler.java` and `backend/src/main/java/com/example/myblog/domain/ApiError.java`
- [X] T016 [P] Implement shared homepage and note detail view models in `backend/src/main/java/com/example/myblog/domain/view/HomePageView.java`, `backend/src/main/java/com/example/myblog/domain/view/FolderView.java`, `backend/src/main/java/com/example/myblog/domain/view/NoteSummaryView.java`, and `backend/src/main/java/com/example/myblog/domain/view/NoteDetailView.java`
- [X] T017 Implement shared read-only note detail query logic in `backend/src/main/java/com/example/myblog/service/NoteQueryService.java`
- [X] T018 Implement the shared note detail read endpoint for `GET /api/notes/{noteId}` in `backend/src/main/java/com/example/myblog/controller/NoteController.java`
- [X] T019 Create test configuration and base integration fixtures in `backend/src/test/resources/application-test.yml`, `backend/src/test/resources/test-data.sql`, and `backend/src/test/java/com/example/myblog/integration/BaseIntegrationTest.java`
- [X] T020 Implement the login page interaction flow in `backend/src/main/resources/static/pages/login.html` and `backend/src/main/resources/static/js/auth.js`
- [X] T021 Implement loading, invalid-credential, and successful-login redirect states in `backend/src/main/resources/static/js/auth.js` and `backend/src/main/resources/static/css/app.css`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Manage Personal Note Homepage (Priority: P1) MVP

**Goal**: Let a logged-in user see their homepage, organize notes by top-level folder, and create a note inside a selected folder

**Independent Test**: Log in with an existing account, create a folder, create a note in that folder, and confirm the homepage shows the note under the correct folder with a working detail link

### Tests for User Story 1 (MANDATORY)

> **NOTE: Write these tests FIRST and ensure they FAIL before implementation**

- [X] T022 [P] [US1] Create the homepage contract test for `GET /api/users/me/home` in `backend/src/test/java/com/example/myblog/contract/HomePageControllerTest.java`
- [X] T023 [P] [US1] Create the folder and note creation contract tests for `POST /api/folders` and `POST /api/notes` in `backend/src/test/java/com/example/myblog/contract/AuthorHomeMutationControllerTest.java`
- [X] T024 [P] [US1] Create the end-to-end author homepage flow test in `backend/src/test/java/com/example/myblog/integration/AuthorHomeFlowTest.java`

### Implementation for User Story 1

- [X] T025 [P] [US1] Implement homepage query and folder creation logic in `backend/src/main/java/com/example/myblog/service/HomePageService.java` and `backend/src/main/java/com/example/myblog/service/FolderService.java`
- [X] T026 [US1] Implement the personal homepage and folder creation endpoints in `backend/src/main/java/com/example/myblog/controller/HomePageController.java` and `backend/src/main/java/com/example/myblog/controller/FolderController.java`
- [X] T027 [US1] Implement the note creation command with folder ownership validation in `backend/src/main/java/com/example/myblog/service/NoteCommandService.java` and `backend/src/main/java/com/example/myblog/controller/NoteController.java`
- [X] T028 [US1] Build the personal homepage UI for folders and note creation in `backend/src/main/resources/static/pages/home.html`, `backend/src/main/resources/static/js/home.js`, and `backend/src/main/resources/static/css/home.css`
- [X] T029 [US1] Add loading, empty, validation-error, and save-success states to the personal homepage UI in `backend/src/main/resources/static/js/home.js` and `backend/src/main/resources/static/css/home.css`

**Checkpoint**: User Story 1 should be fully functional and independently testable

---

## Phase 4: User Story 2 - Author Markdown Editing (Priority: P2)

**Goal**: Let an author open their own note, switch between browse and edit modes, save Markdown, and view rendered content with images

**Independent Test**: Open an owned note, switch to edit mode, save Markdown with headings, code, and image references, then switch back to display mode and verify the rendered result and folder assignment

### Tests for User Story 2 (MANDATORY)

- [X] T030 [P] [US2] Create the note update contract test for `PUT /api/notes/{noteId}` in `backend/src/test/java/com/example/myblog/contract/NoteUpdateControllerTest.java`
- [X] T031 [P] [US2] Create the Markdown rendering and note update business-rule tests in `backend/src/test/java/com/example/myblog/unit/MarkdownRenderServiceTest.java` and `backend/src/test/java/com/example/myblog/mapper/NoteMapperTest.java`
- [X] T032 [P] [US2] Create the author note editing integration flow test in `backend/src/test/java/com/example/myblog/integration/NoteEditingFlowTest.java`

### Implementation for User Story 2

- [X] T033 [P] [US2] Implement backend Markdown rendering and sanitization in `backend/src/main/java/com/example/myblog/service/MarkdownRenderService.java`
- [X] T034 [P] [US2] Implement note update request and save response models in `backend/src/main/java/com/example/myblog/domain/command/UpdateNoteRequest.java` and `backend/src/main/java/com/example/myblog/domain/view/NoteSavedView.java`
- [X] T035 [US2] Implement note update business logic in `backend/src/main/java/com/example/myblog/service/NoteCommandService.java`
- [X] T036 [US2] Complete the author note update endpoint in `backend/src/main/java/com/example/myblog/controller/NoteController.java`
- [X] T037 [US2] Build the note detail page with browse/edit mode switching in `backend/src/main/resources/static/pages/note.html`, `backend/src/main/resources/static/js/note.js`, and `backend/src/main/resources/static/css/note.css`
- [X] T038 [US2] Add loading, edit-mode, save-success, and render-error states to the note detail UI in `backend/src/main/resources/static/js/note.js` and `backend/src/main/resources/static/css/note.css`

**Checkpoint**: User Stories 1 and 2 should both work independently, with author editing fully enabled

---

## Phase 5: User Story 3 - Search and Read Other Users' Notes (Priority: P3)

**Goal**: Let a logged-in reader search other accounts, enter another user's homepage, and browse their notes in read-only mode

**Independent Test**: Log in as a second user, search by a partial account string, open another user's homepage, open a note, and confirm the page is readable but not editable

### Tests for User Story 3 (MANDATORY)

- [X] T039 [P] [US3] Create the user search and foreign homepage contract tests for `GET /api/users/search` and `GET /api/users/{account}/home` in `backend/src/test/java/com/example/myblog/contract/UserDirectoryControllerTest.java`
- [X] T040 [P] [US3] Create the read-only note detail contract and permission test for `GET /api/notes/{noteId}` in `backend/src/test/java/com/example/myblog/contract/ReaderPermissionControllerTest.java`
- [X] T041 [P] [US3] Create the search-to-read integration flow test in `backend/src/test/java/com/example/myblog/integration/ReaderSearchFlowTest.java`

### Implementation for User Story 3

- [X] T042 [P] [US3] Implement fuzzy user search and foreign-homepage query logic in `backend/src/main/java/com/example/myblog/service/UserDirectoryService.java` and `backend/src/main/java/com/example/myblog/service/HomePageService.java`
- [X] T043 [US3] Implement the user search and foreign-homepage endpoints in `backend/src/main/java/com/example/myblog/controller/UserDirectoryController.java` and `backend/src/main/java/com/example/myblog/controller/HomePageController.java`
- [X] T044 [US3] Enforce non-author read-only access rules on the shared note detail endpoint in `backend/src/main/java/com/example/myblog/service/NoteQueryService.java` and `backend/src/main/java/com/example/myblog/controller/NoteController.java`
- [X] T045 [US3] Build the account search and foreign-homepage browsing UI with no-results and read-only states in `backend/src/main/resources/static/pages/home.html`, `backend/src/main/resources/static/js/home-search.js`, and `backend/src/main/resources/static/css/home.css`

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T046 [P] Add semantic structure, form labels, and keyboard focus states to `backend/src/main/resources/static/pages/login.html`, `backend/src/main/resources/static/pages/home.html`, and `backend/src/main/resources/static/css/home.css`
- [X] T047 [P] Add accessible mode toggles and status messaging to `backend/src/main/resources/static/pages/note.html`, `backend/src/main/resources/static/js/note.js`, and `backend/src/main/resources/static/css/note.css`
- [X] T048 [P] Validate keyboard navigation and visible focus behavior in `backend/src/test/java/com/example/myblog/integration/AccessibilitySmokeTest.java`
- [X] T049 [P] Add response-feedback timing checks for homepage, search, and note detail in `backend/src/test/java/com/example/myblog/integration/FeedbackTimingTest.java`
- [X] T050 Harden Markdown image-fallback handling and unauthorized edit error messaging in `backend/src/main/java/com/example/myblog/service/MarkdownRenderService.java` and `backend/src/main/java/com/example/myblog/controller/ApiExceptionHandler.java`
- [X] T051 [P] Update local run and validation guidance in `backend/README.md` and `specs/001-blog-note-sharing/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 because author editing extends note creation and author-owned navigation
- **User Story 3 (Phase 5)**: Depends only on Foundational completion plus shared read-only note querying from Phase 2
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First shippable MVP after foundational work
- **User Story 2 (P2)**: Builds on note creation and homepage navigation from US1
- **User Story 3 (P3)**: Uses the shared note model and read-only detail path, and does not depend on author editing

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Shared models before services
- Services before controllers
- Backend endpoints before browser integration
- Loading, empty, error, success, and accessibility states before story sign-off

### Parallel Opportunities

- `T003`, `T004`, and `T005` can run in parallel after `T001`
- `T007`, `T008`, and `T009` can run in parallel after `T006`
- `T010`, `T011`, and `T012` can run in parallel before auth implementation
- `T022`, `T023`, and `T024` can run in parallel within US1
- `T030`, `T031`, and `T032` can run in parallel within US2
- `T039`, `T040`, and `T041` can run in parallel within US3
- `T046`, `T047`, `T048`, `T049`, and `T051` can run in parallel during polish

---

## Parallel Example: User Story 1

```bash
Task: "T022 [US1] Create the homepage contract test in backend/src/test/java/com/example/myblog/contract/HomePageControllerTest.java"
Task: "T023 [US1] Create the folder and note creation contract tests in backend/src/test/java/com/example/myblog/contract/AuthorHomeMutationControllerTest.java"
Task: "T024 [US1] Create the end-to-end author homepage flow test in backend/src/test/java/com/example/myblog/integration/AuthorHomeFlowTest.java"

Task: "T025 [US1] Implement homepage query and folder creation logic in backend/src/main/java/com/example/myblog/service/HomePageService.java and FolderService.java"
Task: "T028 [US1] Build the personal homepage UI in backend/src/main/resources/static/pages/home.html, backend/src/main/resources/static/js/home.js, and backend/src/main/resources/static/css/home.css"
```

## Parallel Example: User Story 2

```bash
Task: "T030 [US2] Create the note update contract test in backend/src/test/java/com/example/myblog/contract/NoteUpdateControllerTest.java"
Task: "T031 [US2] Create the Markdown rendering and note update business-rule tests in backend/src/test/java/com/example/myblog/unit/MarkdownRenderServiceTest.java and backend/src/test/java/com/example/myblog/mapper/NoteMapperTest.java"
Task: "T032 [US2] Create the author note editing integration flow test in backend/src/test/java/com/example/myblog/integration/NoteEditingFlowTest.java"

Task: "T033 [US2] Implement backend Markdown rendering and sanitization in backend/src/main/java/com/example/myblog/service/MarkdownRenderService.java"
Task: "T034 [US2] Implement note update request and save response models in backend/src/main/java/com/example/myblog/domain/command/UpdateNoteRequest.java and backend/src/main/java/com/example/myblog/domain/view/NoteSavedView.java"
```

## Parallel Example: User Story 3

```bash
Task: "T039 [US3] Create the user search and foreign homepage contract tests in backend/src/test/java/com/example/myblog/contract/UserDirectoryControllerTest.java"
Task: "T040 [US3] Create the read-only note detail contract and permission test in backend/src/test/java/com/example/myblog/contract/ReaderPermissionControllerTest.java"
Task: "T041 [US3] Create the search-to-read integration flow test in backend/src/test/java/com/example/myblog/integration/ReaderSearchFlowTest.java"

Task: "T042 [US3] Implement fuzzy user search and foreign-homepage query logic in backend/src/main/java/com/example/myblog/service/UserDirectoryService.java and backend/src/main/java/com/example/myblog/service/HomePageService.java"
Task: "T045 [US3] Build the account search and foreign-homepage browsing UI in backend/src/main/resources/static/pages/home.html, backend/src/main/resources/static/js/home-search.js, and backend/src/main/resources/static/css/home.css"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. Stop and validate the personal homepage flow
5. Demo the MVP with login, folder creation, note creation, and homepage browsing

### Incremental Delivery

1. Deliver US1 to establish the authenticated homepage and note ownership model
2. Deliver US2 to add author editing, Markdown rendering, and note-detail UX
3. Deliver US3 independently on top of the shared read-only note query path
4. Finish with accessibility, timing validation, smoke coverage, and documentation

### Suggested MVP Scope

- Setup and Foundational phases
- User Story 1 only

---

## Notes

- Total tasks: 51
- User Story task counts: US1 = 8, US2 = 9, US3 = 7
- All tasks follow the required checklist format with checkbox, task ID, story label where needed, and concrete file paths
- The task list now includes explicit auth TDD, accessibility validation, and measurable timing checks for user feedback
