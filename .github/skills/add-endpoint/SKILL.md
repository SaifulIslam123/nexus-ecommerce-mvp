---
name: add-endpoint
description: Add a new REST endpoint to the Nexus API following project conventions.
---
1. Read specs/constitution.md and the matching module under modules/<name>/.
2. Add request/response DTOs in model/dto with Jakarta validation (@field: targets).
3. Put logic in the service; the controller only maps and delegates.
4. Add an access annotation (@IsAdmin, @IsShopper, @IsCourier) or document why it is public.
5. Return a DTO (never an entity). Use ResponseEntity<ApiResponse<Unit>> only to set a status.
6. Add OpenAPI annotations (@Operation, @ApiResponses).
7. Throw existing exceptions (ResourceNotFoundException, BusinessValidationException).
8. Run ./gradlew build.