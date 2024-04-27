-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema letters
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema letters
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `letters` DEFAULT CHARACTER SET utf8 ;
USE `letters` ;

-- -----------------------------------------------------
-- Table `letters`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `letters`.`user` (
  `id` INT NOT NULL,
  `fio` VARCHAR(45) NULL,
  `date_of_birth` DATE NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `letters`.`letter`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `letters`.`letter` (
  `id` INT NOT NULL,
  `sender` INT NOT NULL,
  `recipient` INT NOT NULL,
  `subject` TEXT NULL,
  `body` MEDIUMTEXT NULL,
  `date_of_sending` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_letter_user_idx` (`sender` ASC) VISIBLE,
  INDEX `fk_letter_user1_idx` (`recipient` ASC) VISIBLE,
  CONSTRAINT `fk_letter_user`
    FOREIGN KEY (`sender`)
    REFERENCES `letters`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_letter_user1`
    FOREIGN KEY (`recipient`)
    REFERENCES `letters`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
