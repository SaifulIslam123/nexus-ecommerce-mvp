# Courier Order Feature — Implementation Steering

This file guides Claude Code when implementing the Courier Order APIs.
Read `.claude/COURIER_ORDER_SPEC.md` for full decisions and rationale.

---

## What to build

Four endpoints in a new `CourierOrderController`, backed by updates to `CourierService`
and `OrderRepository`. No new migrations, no new DTOs, no changes to AdminOrderController
or ShopperOrderController.

---

## Files to touch (and only these)

| Action | File |
|---|---|
| CREATE | `src/main/kotlin/com/ecommerce/mvp/modules/courier/CourierOrderController.kt` |
| MODIFY | `src/main/kotlin/com/ecommerce/mvp/modules/courier/service/CourierService.kt` |
| MODIFY | `src/main/kotlin/com/ecommerce/mvp/modules/order/repository/OrderRepository.kt` |

Do **not** touch `OrderService`, `AdminOrderController`, `ShopperOrderController`,
any entity, any DTO, or any Flyway migration file.

---

## Patterns to follow exactly

### Controller shape — mirror AdminOrderController
```kotlin
@RestController
@RequestMapping("/api/v1/courier/orders")
@PreAuthorize("hasRole('COURIER')")          // class-level, not per-method
class CourierOrderController(
    private val courierService: CourierService
) { ... }
```

### Pagination — mirror AdminOrderController
```kotlin
fun listOrders(
    @RequestParam status: OrderStatus? = null,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "10") size: Int
): Page<OrderResponseDto>
```

### Exception types — use existing ones only
- `ResourceNotFoundException("Order not found with id: $id")`
- `BusinessValidationException("...")`

Do **not** throw raw `IllegalStateException` or `IllegalArgumentException`.

### Transactional boundaries
Each mutating service method (`markAsOutForDelivery`, `markAsDelivered`) must be a single
`@Transactional` function. Do not split status update and Courier record creation into
separate transactions.

---

## Critical implementation notes

1. **`findByIdWithShipment` vs `findById`**: `markAsOutForDelivery` must use
   `orderRepository.findByIdWithShipment(orderId)` so `order.shipment` is
   eagerly loaded and its `trackingId` is accessible without a second query.
   After updating status, reload with `findOrderByIdAdmin(id)` before calling
   `toResponseDto()` so all associations (user, items, payment) are present.

2. **Hibernate DISTINCT + Pageable warning**: Adding a `JOIN FETCH` on the `orderItems`
   collection with `Pageable` will trigger a Hibernate warning
   (`HHH90003004: firstResult/maxResults specified with collection fetch`).
   Solve this by either:
   - Using a `countQuery` on `@Query` and a separate fetch query, OR
   - Fetching a page of order IDs first, then loading full details in a second query.
   Do not ignore the warning — it causes incorrect pagination.

3. **Courier record on `markAsDelivered`**: Load the existing record with
   `courierRepository.findByOrderId(orderId)`, then call `courierRepository.save(it)`
   unchanged. This is the minimal touch to trigger `@LastModifiedBy` + `@LastModifiedDate`
   from `BaseEntityAudit`.

4. **Allowed statuses validation on list endpoint**: Before building the query, check:
   ```kotlin
   if (status != null && status !in COURIER_VISIBLE_STATUSES) {
       throw BusinessValidationException("Courier cannot query orders with status $status")
   }
   ```

5. **Address formatting**: `order.shipment!!.shipmentAddress` is lazy. Access it within the
   same `@Transactional` method. Check actual field names on the `Address` entity before
   formatting the string — do not assume field names.

---

## Things to avoid

- Do **not** add `@PreAuthorize` per-method if it's already at class level.
- Do **not** add `@Valid` to path-variable-only endpoints — there is no request body.
- Do **not** add comments explaining *what* the code does; only add comments for
  non-obvious *why* (e.g. the two-step fetch workaround for Hibernate).
- Do **not** create a new `CourierOrderResponseDto` — reuse `OrderResponseDto`.
- Do **not** delete or modify the existing `CourierService.getOrderByStatus()` until
  the controller is wired to the new paginated method; then replace it.
- Do **not** write to the `couriers` table more than once per order
  (guard with `courierRepository.existsByOrderId(orderId)` if needed).

---

## Done checklist

- [ ] `CourierOrderController` has all 4 endpoints, class-level `@PreAuthorize`
- [ ] List endpoint returns `Page<OrderResponseDto>`, filters by allowed statuses only
- [ ] `markAsOutForDelivery` creates a `Courier` record with non-null `trackingId`, `shipmentDate`, `shipmentAddress`
- [ ] `markAsDelivered` touches the existing `Courier` record for audit
- [ ] Both mutation endpoints return a fully-populated `OrderResponseDto`
- [ ] Hibernate pagination warning is resolved
- [ ] No existing tests are broken (`./mvnw test`)
- [ ] App starts cleanly on dev profile (`./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"`)
