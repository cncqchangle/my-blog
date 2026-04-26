# Data Model: Personal Blog Notes

## UserAccount

**Purpose**: Represents an authenticated user with a unique account string and
an owned personal homepage.

**Fields**

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| id | bigint | Yes | Primary key |
| account | varchar(64) | Yes | Unique, non-empty, case-insensitive lookup key |
| password_hash | varchar(255) | Yes | Stored as secure hash, never returned to clients |
| created_at | datetime | Yes | Set on creation |
| updated_at | datetime | Yes | Updated on account changes |

**Relationships**

- One `UserAccount` owns many `Folder` records.
- One `UserAccount` authors many `Note` records.

**Validation**

- `account` must be unique across the system.
- `account` must be present before login or search can succeed.

## Folder

**Purpose**: Represents one top-level grouping of notes for a single user.

**Fields**

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| id | bigint | Yes | Primary key |
| owner_user_id | bigint | Yes | Foreign key to `UserAccount.id` |
| name | varchar(100) | Yes | Non-empty, unique per owner |
| display_order | int | Yes | Non-negative integer for stable homepage ordering |
| created_at | datetime | Yes | Set on creation |
| updated_at | datetime | Yes | Updated on rename or reorder |

**Relationships**

- One `Folder` belongs to exactly one `UserAccount`.
- One `Folder` contains many `Note` records.

**Validation**

- Folder names must be unique within the same user's homepage.
- Folder deletion is blocked while notes still belong to it unless note
  reassignment is explicitly handled by a future feature.

## Note

**Purpose**: Represents a published Markdown note that can be edited by its
author and read by other authenticated users.

**Fields**

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| id | bigint | Yes | Primary key |
| author_user_id | bigint | Yes | Foreign key to `UserAccount.id` |
| folder_id | bigint | Yes | Foreign key to `Folder.id` |
| title | varchar(200) | Yes | Non-empty |
| markdown_content | longtext | Yes | Canonical editable note body |
| rendered_html | longtext | Yes | Sanitized HTML derived from Markdown |
| publication_status | varchar(20) | Yes | `PUBLISHED` in current scope |
| created_at | datetime | Yes | Set on creation |
| updated_at | datetime | Yes | Updated on each successful edit |

**Relationships**

- One `Note` belongs to exactly one `Folder`.
- One `Note` is authored by exactly one `UserAccount`.

**Validation**

- Every note must reference a folder owned by the same author.
- `title` and `markdown_content` must be present before save succeeds.
- Non-authors cannot update `title`, `markdown_content`, or `folder_id`.
- `rendered_html` must be regenerated whenever `markdown_content` changes.

**State transitions**

| From | To | Trigger |
|------|----|---------|
| New | PUBLISHED | Author creates and saves a note with valid folder assignment |
| PUBLISHED | PUBLISHED | Author edits title/content/folder and saves successfully |

## Derived View Models

These are not separate tables, but they shape API responses and UI behavior.

### HomePageView

- `ownerAccount`
- `isViewerOwner`
- `folders[]`
  - `folderId`
  - `folderName`
  - `noteCount`
  - `notes[]`
    - `noteId`
    - `title`
    - `updatedAt`

### NoteDetailView

- `noteId`
- `authorAccount`
- `folderId`
- `folderName`
- `title`
- `markdownContent`
- `renderedHtml`
- `isEditable`
- `updatedAt`

### UserSearchResult

- `account`
- `matchSnippet` (optional)
- `isExactMatch`

## Persistence Notes

- Keep referential integrity in MySQL with foreign keys between users, folders,
  and notes.
- Add an index on `user_account.account` for login and search.
- Add a composite index on `folder.owner_user_id, folder.display_order`.
- Add a composite index on `note.author_user_id, note.folder_id, note.updated_at`
  to support homepage loading.
