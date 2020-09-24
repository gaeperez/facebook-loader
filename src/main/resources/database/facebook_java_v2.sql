-- MySQL Script generated by MySQL Workbench
-- Wed May 27 10:20:43 2020
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema facebook_java
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `facebook_java` ;

-- -----------------------------------------------------
-- Schema facebook_java
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `facebook_java` DEFAULT CHARACTER SET utf8mb4 ;
USE `facebook_java` ;

-- -----------------------------------------------------
-- Table `facebook_java`.`page`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`page` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`page` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `external_id` VARCHAR(50) NOT NULL COMMENT 'Facebook ID',
  `name` VARCHAR(255) NOT NULL,
  `link` VARCHAR(255) NOT NULL,
  `parsed` TINYINT NOT NULL COMMENT 'True: all the information of the page was already retrieved\nFalse: information is missing',
  `about` VARCHAR(255) NULL,
  `checkins` INT NULL,
  `category` VARCHAR(255) NULL,
  `description` TEXT NULL,
  `engagement` INT NULL,
  `general_info` TEXT NULL,
  `impressum` VARCHAR(2000) NULL,
  `phone` VARCHAR(80) NULL,
  `website` VARCHAR(255) NULL,
  `single_line_address` VARCHAR(300) NULL,
  `products` VARCHAR(500) NULL,
  `price_range` VARCHAR(10) NULL,
  `overall_star_rating` DOUBLE NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`category_list`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`category_list` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`category_list` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `external_id` VARCHAR(50) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`page_has_category_list`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`page_has_category_list` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`page_has_category_list` (
  `page_id` INT NOT NULL,
  `category_id` INT NOT NULL,
  PRIMARY KEY (`page_id`, `category_id`),
  INDEX `fk_page_has_category_category1_idx` (`category_id` ASC),
  INDEX `fk_page_has_category_page1_idx` (`page_id` ASC),
  CONSTRAINT `fk_page_has_category_page1`
    FOREIGN KEY (`page_id`)
    REFERENCES `facebook_java`.`page` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_page_has_category_category1`
    FOREIGN KEY (`category_id`)
    REFERENCES `facebook_java`.`category_list` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`email`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`email` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`email` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`page_has_email`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`page_has_email` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`page_has_email` (
  `page_id` INT NOT NULL,
  `email_id` INT NOT NULL,
  PRIMARY KEY (`page_id`, `email_id`),
  INDEX `fk_page_has_email_email1_idx` (`email_id` ASC),
  INDEX `fk_page_has_email_page1_idx` (`page_id` ASC),
  CONSTRAINT `fk_page_has_email_page1`
    FOREIGN KEY (`page_id`)
    REFERENCES `facebook_java`.`page` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_page_has_email_email1`
    FOREIGN KEY (`email_id`)
    REFERENCES `facebook_java`.`email` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`hour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`hour` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`hour` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `page_id` INT NOT NULL,
  `day_of_week` VARCHAR(3) NOT NULL COMMENT 'MON, WED...',
  `open_time` TIME NOT NULL,
  `close_time` TIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_hour_page1_idx` (`page_id` ASC),
  CONSTRAINT `fk_hour_page1`
    FOREIGN KEY (`page_id`)
    REFERENCES `facebook_java`.`page` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`insights`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`insights` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`insights` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `inserted` DATETIME NOT NULL COMMENT 'Inserted in database',
  `created` DATETIME NOT NULL COMMENT 'Created in Facebook',
  `hash` VARCHAR(255) NOT NULL,
  `response` JSON NOT NULL COMMENT 'Created + externalID + response',
  `external_id` VARCHAR(50) NOT NULL COMMENT 'External ID related to the object in the URL (e.g. page id)',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `hash_UNIQUE` (`hash` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`location`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`location` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`location` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `city` VARCHAR(100) NULL,
  `country` VARCHAR(100) NULL,
  `street` VARCHAR(255) NULL,
  `latitude` DOUBLE NULL,
  `longitude` DOUBLE NULL,
  `zip` VARCHAR(50) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`place`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`place` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`place` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `location_id` INT NOT NULL,
  `external_id` VARCHAR(50) NOT NULL,
  `name` VARCHAR(500) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC),
  INDEX `fk_place_location1_idx` (`location_id` ASC),
  CONSTRAINT `fk_place_location1`
    FOREIGN KEY (`location_id`)
    REFERENCES `facebook_java`.`location` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`node`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`node` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`node` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'A Node can be a Post or a Video',
  `page_id` INT NOT NULL,
  `place_id` INT NULL,
  `external_id` VARCHAR(50) NOT NULL,
  `created` DATETIME NOT NULL,
  `updated` DATETIME NOT NULL,
  `keep_updating` TINYINT NOT NULL COMMENT 'False/True',
  `parsed` TINYINT NOT NULL,
  `perma_link` VARCHAR(500) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC),
  INDEX `fk_node_page1_idx` (`page_id` ASC),
  INDEX `fk_node_place1_idx` (`place_id` ASC),
  CONSTRAINT `fk_node_page1`
    FOREIGN KEY (`page_id`)
    REFERENCES `facebook_java`.`page` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_node_place1`
    FOREIGN KEY (`place_id`)
    REFERENCES `facebook_java`.`place` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`post`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`post` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`post` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `link` VARCHAR(2000) NULL,
  `message` TEXT NULL,
  `status_type` VARCHAR(100) NULL,
  `story` TEXT NULL,
  `shares` INT NULL COMMENT 'TODO: provisional field, cannot check now',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_post_node1`
    FOREIGN KEY (`id`)
    REFERENCES `facebook_java`.`node` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`message_tag`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`message_tag` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`message_tag` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `post_id` INT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_message_tags_post1_idx` (`post_id` ASC),
  CONSTRAINT `fk_message_tags_post1`
    FOREIGN KEY (`post_id`)
    REFERENCES `facebook_java`.`post` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`attachment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`attachment` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`attachment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(100) NOT NULL,
  `url` VARCHAR(1000) NOT NULL,
  `title` VARCHAR(500) NULL,
  `description` TEXT NULL,
  `media_type` VARCHAR(50) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`comment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`comment` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`comment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `attachment_id` INT NULL,
  `parent_id` INT NULL COMMENT 'For replies',
  `external_id` VARCHAR(50) NOT NULL,
  `created` DATETIME NOT NULL,
  `message` TEXT NULL,
  `like_count` INT NULL,
  `comment_count` INT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC),
  INDEX `fk_comment_attachment1_idx` (`attachment_id` ASC),
  INDEX `fk_comment_comment1_idx` (`parent_id` ASC),
  CONSTRAINT `fk_comment_attachment1`
    FOREIGN KEY (`attachment_id`)
    REFERENCES `facebook_java`.`attachment` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_comment_comment1`
    FOREIGN KEY (`parent_id`)
    REFERENCES `facebook_java`.`comment` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`video`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`video` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`video` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `source` VARCHAR(2000) NOT NULL,
  `length` FLOAT NOT NULL,
  `title` VARCHAR(500) NULL,
  `description` TEXT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_video_node1`
    FOREIGN KEY (`id`)
    REFERENCES `facebook_java`.`node` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`custom_label`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`custom_label` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`custom_label` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `video_id` INT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_custom_label_video1_idx` (`video_id` ASC),
  CONSTRAINT `fk_custom_label_video1`
    FOREIGN KEY (`video_id`)
    REFERENCES `facebook_java`.`video` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `facebook_java`.`node_has_comment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `facebook_java`.`node_has_comment` ;

CREATE TABLE IF NOT EXISTS `facebook_java`.`node_has_comment` (
  `node_id` INT NOT NULL,
  `comment_id` INT NOT NULL,
  PRIMARY KEY (`node_id`, `comment_id`),
  INDEX `fk_node_has_comment_comment1_idx` (`comment_id` ASC),
  INDEX `fk_node_has_comment_node1_idx` (`node_id` ASC),
  CONSTRAINT `fk_node_has_comment_node1`
    FOREIGN KEY (`node_id`)
    REFERENCES `facebook_java`.`node` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_node_has_comment_comment1`
    FOREIGN KEY (`comment_id`)
    REFERENCES `facebook_java`.`comment` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;