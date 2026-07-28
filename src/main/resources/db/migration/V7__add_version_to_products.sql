-- V7__add_version_to_products.sql
-- Add optimistic locking `version` column to products table for JPA @Version
-- This migration is for MySQL 8

ALTER TABLE `products`
  ADD COLUMN `version` BIGINT NOT NULL DEFAULT 0;

