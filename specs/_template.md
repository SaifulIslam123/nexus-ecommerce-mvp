# Feature: <short name>

## Goal
One or two sentences: what problem this solves and for whom.

## Who can use it
- Role(s): public / SHOPPER / ADMIN / COURIER
- Auth: JWT required? (yes/no)

## Endpoints
| Method | Path | Access | Success | Errors |
| --- | --- | --- | --- | --- |
| POST | /api/v1/... | @IsAdmin | 201 + DTO | 400 invalid input, 404 not found |

## Request and response
- Request fields, types, validation rules (required, size, format)
- Response fields (DTO only, never entities)
- Example request and response (JSON)

## Business rules
- Numbered rules, one sentence each (for example "Stock cannot go below 0")
- Edge cases and what should happen in each

## Data changes
- New or changed tables and columns (next Flyway migration is V9)
- Indexes, uniqueness, foreign keys, delete behaviour

## Security and privacy
- Rate limits (if any)
- What must never be logged or returned
- Anything that must not reveal whether an account or record exists

## Out of scope
- What this feature must NOT do or change (stops unrelated refactors)

## Acceptance checks
- [ ] Checklist anyone can verify, written as observable behaviour
- [ ] Tests cover: success path, validation error, unauthorized, forbidden
- [ ] `./gradlew build` passes

## Open questions
- Anything unsure. The planner will ask about these.