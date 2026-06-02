# Courier Order APIs — Architectural Blueprint

## 1. Context & Scope

Builds `CourierOrderController` (replacing the deleted `CourierController`) to give COURIER-role
users a clean, secured API for their two delivery operations. All other modules (OrderService,
AdminOrderController, ShopperOrderController) are unaffected.

### Decisions locked in

| Decision | Choice |
|---|---|
| Assignment model | Any COURIER can act on any eligible order — no per-courier scoping |
| Visible statuses | SHIPPED, OUT_FOR_DELIVERY, DELIVERED |
| List style | Paginated + optional `?status=` filter (mirrors AdminOrderController) |
| Couriers table | Written on `markAsOutForDelivery`; `BaseEntityAudit.modifiedBy` tracks `markAsDelivered` |

---

## 2. API Endpoints

Base path: `/api/v1/courier/orders`
Security: `@PreAuthorize("hasRole('COURIER')")` at **class level**

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/courier/orders` | Paginated order list; optional `?status=` filter |
| `GET` | `/api/v1/courier/orders/{id}` | Single order by ID |
| `PUT` | `/api/v1/courier/orders/{id}/out-for-delivery` | SHIPPED → OUT_FOR_DELIVERY |
| `PUT` | `/api/v1/courier/orders/{id}/delivered` | OUT_FOR_DELIVERY → DELIVERED |

---

## 3. Endpoint Details

### GET /api/v1/courier/orders

Query params:

| Param | Type | Default | Notes |
|---|---|---|---|
| `status` | `OrderStatus?` | `null` | One of `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`. If omitted, all three are returned. Reject other statuses with 400. |
| `page` | `Int` | `0` | Zero-based page index |
| `size` | `Int` | `10` | Page size |

Response: `Page<OrderResponseDto>`

When `status` is null, use a multi-status IN query.  
When `status` is supplied, validate it is within the allowed set; return `BusinessValidationException` if not.

---

### GET /api/v1/courier/orders/{id}

Returns any single order regardless of status.  
Response: `OrderResponseDto`  
Error: 404 if not found.

---

### PUT /api/v1/courier/orders/{id}/out-for-delivery

Transition: `SHIPPED → OUT_FOR_DELIVERY`

**Side effects (in one `@Transactional`):**
1. Load order with shipment via `findByIdWithShipment` (to access `order.shipment.trackingId`)
2. Validate `order.status == SHIPPED`; throw `BusinessValidationException` if not
3. Validate `order.shipment != null`; throw `BusinessValidationException("Order has no shipment record")` if null
4. Set `order.status = OUT_FOR_DELIVERY`
5. Create and save a `Courier` record:
   - `orderId` = `order.id!!`
   - `trackingId` = `order.shipment!!.trackingId!!`
   - `shipmentDate` = `Instant.now()`
   - `shipmentAddress` = formatted address string (see §6)
6. Return full `OrderResponseDto` by reloading via `findOrderByIdAdmin(id)` (loads user, items, payment, shipment)

Response: `OrderResponseDto`  
Errors: 404 order not found · 409 wrong state · 409 no shipment record

---

### PUT /api/v1/courier/orders/{id}/delivered

Transition: `OUT_FOR_DELIVERY → DELIVERED`

**Side effects (in one `@Transactional`):**
1. Load order; validate `order.status == OUT_FOR_DELIVERY`
2. Set `order.status = DELIVERED`
3. Load the existing `Courier` record via `courierRepository.findByOrderId(id)` and save a trivial
   update so `BaseEntityAudit.modifiedBy` + `modifiedDate` are stamped with the acting courier's identity
4. Return full `OrderResponseDto`

Response: `OrderResponseDto`  
Errors: 404 order not found · 409 wrong state

---

## 4. Security

```kotlin
@RestController
@RequestMapping("/api/v1/courier/orders")
@PreAuthorize("hasRole('COURIER')")
class CourierOrderController(...)
```

JWT tokens already embed roles; no DB lookup on each request. The `@CreatedBy` / `@LastModifiedBy`
fields in `BaseEntityAudit` are automatically populated by Spring Data's `AuditorAware` from the
Security context — this is how courier identity is captured in the `couriers` table without a
`user_id` FK.

---

## 5. Files to Create / Modify

### Create
```
src/main/kotlin/com/ecommerce/mvp/modules/courier/CourierOrderController.kt
```

### Modify
```
src/main/kotlin/com/ecommerce/mvp/modules/courier/service/CourierService.kt
src/main/kotlin/com/ecommerce/mvp/modules/order/repository/OrderRepository.kt
```

**No new Flyway migration** — `couriers` table, all roles, and all `OrderStatus` values already exist.

---

## 6. Service Layer Changes (CourierService)

### New / replaced methods

```
getOrdersForCourier(status: OrderStatus?, page: Int, size: Int): Page<OrderResponseDto>
```
- If `status` is non-null, validate it is in `COURIER_VISIBLE_STATUSES`
- Delegate to the appropriate OrderRepository query

```
getOrderByIdForCourier(id: Long): OrderResponseDto
```
- Use `orderRepository.findOrderByIdAdmin(id)` (reuse existing query)
- Throw `ResourceNotFoundException` if null

```
markAsOutForDelivery(orderId: Long): OrderResponseDto   // replace existing
```
- Use `findByIdWithShipment` for the transition + Courier record creation
- After save, reload via `findOrderByIdAdmin` for the full DTO

```
markAsDelivered(orderId: Long): OrderResponseDto        // replace existing
```
- Load + validate + update status
- Touch existing Courier record (fetch + save) so audit fields update

### Constant
```kotlin
private val COURIER_VISIBLE_STATUSES = setOf(
    OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED
)
```

### Courier record shipmentAddress formatting

`order.shipment!!.shipmentAddress` is an `Address` entity (ManyToOne on Shipment, lazy). Access it
within the same `@Transactional` call or use a JOIN FETCH query. Format as:

```kotlin
val addr = order.shipment!!.shipmentAddress
val addressString = "${addr.street}, ${addr.city}, ${addr.state} ${addr.postalCode}"
```

Adjust field names to match the actual `Address` entity fields.

---

## 7. Repository Changes (OrderRepository)

Add two new methods:

```kotlin
// For paginated single-status query
fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order>

// For paginated multi-status query (status omitted → all three)
@Query("""
    SELECT DISTINCT o FROM Order o
    JOIN FETCH o.user
    JOIN FETCH o.orderItems item
    JOIN FETCH item.product
    LEFT JOIN FETCH o.payment
    LEFT JOIN FETCH o.shipment
    WHERE o.status IN :statuses
    ORDER BY o.orderDate DESC
""")
fun findByStatusIn(
    @Param("statuses") statuses: Collection<OrderStatus>,
    pageable: Pageable
): Page<Order>
```

> **Note on DISTINCT + Pageable:** Spring Data JPA cannot apply SQL LIMIT/OFFSET when a
> `JOIN FETCH` on a collection (`orderItems`) is combined with `Pageable`. Use
> `countQuery` parameter on `@Query` or switch to two queries (fetch page of IDs first,
> then fetch details). This is a known Hibernate limitation — address it during implementation.

---

## 8. Couriers Table — Audit Record Lifecycle

| Event | couriers table action |
|---|---|
| `markAsOutForDelivery` called | INSERT new `Courier` row; `created_by` = COURIER user email |
| `markAsDelivered` called | SELECT existing row → save unchanged → `modified_by` = COURIER user email, `modified_date` = now |

The `couriers` table has a UNIQUE constraint on `tracking_id`. Since tracking IDs come from the
`shipments` table (also unique), there is no risk of collision.

---

## 9. Error Scenarios

| Scenario | Exception | HTTP |
|---|---|---|
| Order ID not found | `ResourceNotFoundException` | 404 |
| Order not in expected status | `BusinessValidationException` | 409 |
| Order has no shipment record on OUT_FOR_DELIVERY | `BusinessValidationException` | 409 |
| `status` query param not in allowed set | `BusinessValidationException` | 400 |
| Courier record already exists for order (duplicate call) | `BusinessValidationException` or DB unique violation | 409 |

---

## 10. Non-Goals

- No ADMIN order operations (those stay in AdminOrderController)
- No shopper-facing changes
- No new database migrations
- No email notifications on status change (those live in OrderService/AdminOrderController if present)
- No courier-to-user assignment table
