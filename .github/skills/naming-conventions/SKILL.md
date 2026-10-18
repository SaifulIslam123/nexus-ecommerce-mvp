---
name: naming-conventions
description: Naming and layout rules for new code in the Nexus project.
---
- Packages: com.ecommerce.mvp.modules.<module>/{controller,service,repository,model/entity,model/dto}
- Classes: XController, XService, XRepository, XDto (entities singular: Product)
- Endpoints: /api/v1/<plural-noun>, kebab-case actions (/forgot-password)
- Tables: snake_case, plural. Migrations: V<n>__snake_case.sql
- Constants live in an object (AppConstant, RateLimitConstants), not magic numbers
- Domain exceptions go in common/exception