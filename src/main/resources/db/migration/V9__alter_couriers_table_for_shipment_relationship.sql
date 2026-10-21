-- 1. Remove the old columns that are no longer present in the entity
ALTER TABLE couriers DROP COLUMN shipment_date;
ALTER TABLE couriers DROP COLUMN tracking_id;
ALTER TABLE couriers DROP COLUMN order_id;
ALTER TABLE couriers DROP COLUMN shipment_address;

-- 2. Add the new relationship column that Hibernate is complaining about
ALTER TABLE couriers ADD COLUMN shipment_id BIGINT UNIQUE;

-- 3. (Optional but highly recommended) Add the foreign key constraint
-- Assumes your shipments table primary key is named 'id'
ALTER TABLE couriers
ADD CONSTRAINT fk_couriers_shipment
FOREIGN KEY (shipment_id) REFERENCES shipments(id);
