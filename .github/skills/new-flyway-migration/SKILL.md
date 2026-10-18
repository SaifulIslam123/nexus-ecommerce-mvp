---
name: new-flyway-migration
description: Create a Flyway migration for a schema change in the Nexus project.
---
1. List src/main/resources/db/migration and use the next version (currently V9).
2. Name it V<n>__snake_case_description.sql. Never edit existing files.
3. Copy the style of V8__create_password_reset_tokens.sql (audit columns for BaseEntityAudit, snake_case plural table names).
4. Update the entity so field names and types match the columns (Hibernate only validates).
5. Run ./gradlew build.