# My Blog Backend

## Run locally

1. Use Java 25 and Maven 3.8+.
2. Configure `APP_DATASOURCE_URL`, `APP_DATASOURCE_USERNAME`, and `APP_DATASOURCE_PASSWORD`.
3. Start the app from `backend/` with:

```bash
mvn spring-boot:run
```

4. Open `/pages/login.html`.

## Test

```bash
mvn test
```

The test profile uses H2 in MySQL compatibility mode and loads `schema.sql` plus `test-data.sql` before test execution.
