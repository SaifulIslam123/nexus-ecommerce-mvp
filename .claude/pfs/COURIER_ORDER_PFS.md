# Product Feature Specification (PFS): Build all the api for Courier role order delivery functions. 

## 1. Executive Vision
This eccommerce backend project has type user role is COURIER. Courier primary responsiblity is to deliver order to delivery location.

## 2. Core functility
When ADMIN put the order to SHIPPED order status state then the COURIER will start it's operation. The main goal of this COURIER is deliver order to the shipping addres. COURIER will mainly help in this in the below two part:
- Order is with the local courier for final delivery is called OrderStatus as OUT_FOR_DELIVERY,
- Order will successfully deliver by the courier to SHOPPER is called OrderStatus as DELIVERED

## 3. Technical & Architectural Guardrails
Need new CourierOrderController which will follow the current project AdminOrderController implementation. 

## 4. Database Entity
- couriers 

## 4. Feature Epics & Acceptance Criteria

### Epic 1: Mark down OUT_FOR_DELIVERY
As a COURIER user, I will mark down courier operation. Transitions a SHIPPED order to OUT_FOR_DELIVERY,
signalling that the package is with the local courier for final delivery.

### Epic 2: Mark down DELIVERED
As a COURIER user, I will mark down courier operation. Transitions an OUT_FOR_DELIVERY order to DELIVERED,
signalling that the package has been successfully received by the customer.

