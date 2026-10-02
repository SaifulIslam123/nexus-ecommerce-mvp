# Constitution

## Conventions
- Follow the existing package layout, naming, and error DTO structure.
- Match the password rules already used in registration validation.
- Schema changes only via new Flyway migrations. Never edit existing ones.
- Config and secrets come from environment properties and `.env.example`.
- All API endpoints must be documented with OpenAPI/Swagger.
- All API endpoints must return a consistent response structure (e.g., `ApiResponse<T>`).
- Never return raw entity objects in API responses. Use DTOs instead.

## Security
- Never log tokens, passwords, or full email addresses.
- Never return stack traces or `ex.message` in API responses.
- Store only hashed reset tokens. Never store raw tokens.
- Responses must not reveal whether an account exists.

## Quality
- No unrelated refactors. Ask when the spec is ambiguous.