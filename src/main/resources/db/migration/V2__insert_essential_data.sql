-- =============================================================
-- Flyway Migration V2 - Essential Seed Data
-- =============================================================

-- -------------------------------------------------------------
-- Roles
-- -------------------------------------------------------------
INSERT INTO `roles` (`id`, `created_by`, `created_date`, `modified_by`, `modified_date`, `name`) VALUES
  (1, 'system', NOW(), 'system', NOW(), 'USER'),
  (2, 'system', NOW(), 'system', NOW(), 'ADMIN'),
  (3, 'system', NOW(), 'system', NOW(), 'MODERATOR'),
  (4, 'system', NOW(), 'system', NOW(), 'COURIER');

-- -------------------------------------------------------------
-- Categories (root categories first, then sub-categories)
-- -------------------------------------------------------------
INSERT INTO `categories` (`id`, `created_by`, `created_date`, `modified_by`, `modified_date`, `name`, `description`, `parent_id`) VALUES
  (1,  'system', NOW(), 'system', NOW(), 'Electronics',     'Consumer electronics, gadgets and accessories', NULL),
  (2,  'system', NOW(), 'system', NOW(), 'Clothing',        'Men''s and women''s fashion apparel',           NULL),
  (3,  'system', NOW(), 'system', NOW(), 'Books',           'Fiction, non-fiction, academic and more',       NULL),
  (4,  'system', NOW(), 'system', NOW(), 'Home & Garden',   'Furniture, décor and outdoor products',         NULL),
  (5,  'system', NOW(), 'system', NOW(), 'Sports & Outdoors','Equipment and gear for every sport',           NULL),
  -- Electronics sub-categories
  (6,  'system', NOW(), 'system', NOW(), 'Mobile Phones',   'Smartphones and feature phones',                1),
  (7,  'system', NOW(), 'system', NOW(), 'Laptops',         'Notebooks and ultrabooks',                      1),
  (8,  'system', NOW(), 'system', NOW(), 'Accessories',     'Cables, cases, chargers and more',              1),
  -- Clothing sub-categories
  (9,  'system', NOW(), 'system', NOW(), 'Men''s Wear',     'Shirts, trousers, jackets for men',             2),
  (10, 'system', NOW(), 'system', NOW(), 'Women''s Wear',   'Dresses, tops, bottoms for women',              2),
  -- Books sub-categories
  (11, 'system', NOW(), 'system', NOW(), 'Fiction',         'Novels, short stories and poetry',              3),
  (12, 'system', NOW(), 'system', NOW(), 'Non-Fiction',     'Biographies, self-help and science',            3),
  -- Home & Garden sub-categories
  (13, 'system', NOW(), 'system', NOW(), 'Furniture',       'Sofas, tables, chairs and beds',                4),
  (14, 'system', NOW(), 'system', NOW(), 'Décor',           'Wall art, lamps and decorative items',          4),
  -- Sports & Outdoors sub-categories
  (15, 'system', NOW(), 'system', NOW(), 'Fitness',         'Gym equipment and accessories',                 5),
  (16, 'system', NOW(), 'system', NOW(), 'Outdoor',         'Camping, hiking and cycling gear',              5);

-- -------------------------------------------------------------
-- Tags
-- -------------------------------------------------------------
INSERT INTO `tags` (`id`, `created_by`, `created_date`, `modified_by`, `modified_date`, `name`) VALUES
  (1, 'system', NOW(), 'system', NOW(), 'New Arrival'),
  (2, 'system', NOW(), 'system', NOW(), 'Bestseller'),
  (3, 'system', NOW(), 'system', NOW(), 'Sale'),
  (4, 'system', NOW(), 'system', NOW(), 'Premium'),
  (5, 'system', NOW(), 'system', NOW(), 'Eco-Friendly');
