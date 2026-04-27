# My Blog Backend

## Run locally

1. Use Java 25 and Maven 3.8+.
2. Configure `APP_DATASOURCE_URL`, `APP_DATASOURCE_USERNAME`, and `APP_DATASOURCE_PASSWORD`.
3. Configure OSS-related environment variables:
   - `APP_OSS_ENDPOINT`
   - `APP_OSS_BUCKET`
   - `APP_OSS_ACCESS_KEY_ID`
   - `APP_OSS_ACCESS_KEY_SECRET`
   - `APP_OSS_DEFAULT_COVER_URL`
4. If this is an existing database, run:

```sql
ALTER TABLE note ADD COLUMN cover_image_url VARCHAR(512) NULL AFTER title;
SOURCE src/main/resources/003-session.sql;
```

5. Start the app from `backend/` with:

```bash
mvn spring-boot:run
```

6. Create and update note requests now use `multipart/form-data` with fields:
   - `title`
   - `markdownContent`
   - `folderId`
   - `coverImage` (optional image file)

7. Open `/pages/login.html`.

## Test

```bash
mvn test
```

The test profile uses H2 in MySQL compatibility mode and loads `schema.sql` plus `test-data.sql` before test execution.

Known issue:

- `AccessibilitySmokeTest` currently expects `src/main/resources/static/pages/home.html` and `note.html`, but the repository only ships a single `static/index.html`. This test still fails independently of the cover-image backend changes.
