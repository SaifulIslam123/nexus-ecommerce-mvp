-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: localhost    Database: ecommerce
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '33c0b63e-bca4-11f0-bedf-68d90692c93c:1-560';

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Saiful','2026-02-20 00:59:44.692000','Saiful','2026-02-20 00:59:44.692000','API Request Body to be sent with the request. We can use variables to dynamically set values in the request body.','Category_1',NULL),(2,'Saiful','2026-03-23 00:15:41.623000','Saiful','2026-03-23 00:15:41.623000','Consumer electronics, gadgets and accessories','Electronics',NULL),(3,'Saiful','2026-03-23 00:15:41.626000','Saiful','2026-03-23 00:15:41.626000','Men\'s and women\'s fashion apparel','Clothing',NULL),(4,'Saiful','2026-03-23 00:15:41.628000','Saiful','2026-03-23 00:15:41.628000','Fiction, non-fiction, academic and more','Books',NULL),(5,'Saiful','2026-03-23 00:15:41.630000','Saiful','2026-03-23 00:15:41.630000','Furniture, décor and outdoor products','Home & Garden',NULL),(6,'Saiful','2026-03-23 00:15:41.631000','Saiful','2026-03-23 00:15:41.631000','Equipment and gear for every sport','Sports & Outdoors',NULL),(7,'Saiful','2026-03-23 00:15:41.633000','Saiful','2026-03-23 00:15:41.633000','Smartphones and feature phones','Mobile Phones',2),(8,'Saiful','2026-03-23 00:15:41.635000','Saiful','2026-03-23 00:15:41.635000','Notebooks and ultrabooks','Laptops',2),(9,'Saiful','2026-03-23 00:15:41.636000','Saiful','2026-03-23 00:15:41.636000','Cables, cases, chargers and more','Accessories',2),(10,'Saiful','2026-03-23 00:15:41.638000','Saiful','2026-03-23 00:15:41.638000','Shirts, trousers, jackets for men','Men\'s Wear',3),(11,'Saiful','2026-03-23 00:15:41.640000','Saiful','2026-03-23 00:15:41.640000','Dresses, tops, bottoms for women','Women\'s Wear',3),(12,'Saiful','2026-03-23 00:15:41.641000','Saiful','2026-03-23 00:15:41.641000','Novels, short stories and poetry','Fiction',4),(13,'Saiful','2026-03-23 00:15:41.643000','Saiful','2026-03-23 00:15:41.643000','Biographies, self-help and science','Non-Fiction',4),(14,'Saiful','2026-03-23 00:15:41.644000','Saiful','2026-03-23 00:15:41.644000','Sofas, tables, chairs and beds','Furniture',5),(15,'Saiful','2026-03-23 00:15:41.645000','Saiful','2026-03-23 00:15:41.645000','Wall art, lamps and decorative items','Décor',5),(16,'Saiful','2026-03-23 00:15:41.647000','Saiful','2026-03-23 00:15:41.647000','Gym equipment and accessories','Fitness',6),(17,'Saiful','2026-03-23 00:15:41.648000','Saiful','2026-03-23 00:15:41.648000','Camping, hiking and cycling gear','Outdoor',6);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `product_tags`
--

LOCK TABLES `product_tags` WRITE;
/*!40000 ALTER TABLE `product_tags` DISABLE KEYS */;
INSERT INTO `product_tags` VALUES (2,1),(3,1),(4,1),(9,1),(12,1),(15,1),(17,1),(23,1),(25,1),(26,1),(28,1),(32,1),(3,2),(6,2),(7,2),(11,2),(13,2),(14,2),(18,2),(20,2),(21,2),(22,2),(23,2),(24,2),(27,2),(30,2),(33,2),(5,3),(11,3),(14,3),(16,3),(17,3),(29,3),(31,3),(2,4),(6,4),(8,4),(10,4),(19,4),(27,4),(28,5),(31,5),(33,5);
/*!40000 ALTER TABLE `product_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Saiful','2025-12-14 22:50:50.733000','Saiful','2025-12-14 22:50:50.733000','USER'),(2,'Saiful','2025-12-14 22:50:50.758000','Saiful','2025-12-14 22:50:50.758000','ADMIN'),(3,'Saiful','2025-12-14 22:50:50.761000','Saiful','2025-12-14 22:50:50.761000','MODERATOR'),(5,'Saiful','2025-12-26 03:34:43.899000','Saiful','2025-12-26 03:34:43.899000','COURIER');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` VALUES (1,'Saiful','2026-03-23 00:15:41.590000','Saiful','2026-03-23 00:15:41.590000','New Arrival'),(2,'Saiful','2026-03-23 00:15:41.612000','Saiful','2026-03-23 00:15:41.612000','Bestseller'),(3,'Saiful','2026-03-23 00:15:41.615000','Saiful','2026-03-23 00:15:41.615000','Sale'),(4,'Saiful','2026-03-23 00:15:41.618000','Saiful','2026-03-23 00:15:41.618000','Premium'),(5,'Saiful','2026-03-23 00:15:41.620000','Saiful','2026-03-23 00:15:41.620000','Eco-Friendly');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (6,1),(11,1),(13,1),(16,1),(19,1),(21,1),(22,1),(23,1),(24,1),(26,1),(4,2),(5,3);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-09 13:56:17
