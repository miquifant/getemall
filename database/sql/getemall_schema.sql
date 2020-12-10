-- ============================================================================================= --
-- Getemall database schema creation script.
--
-- Created by miquifant on 2020-11-09
-- ============================================================================================= --

-- ============================================================================================= --
-- PLATFORM TABLES: Managed by getemall admins
-- ============================================================================================= --

-- platform admin -> role: admin     | This superpowers has nothing to do with the privileges
--   regular user -> no super powers | inside an organization, a game, a match, etc.
--      anonymous -> no super powers | Those are managed by organization/games/matches
CREATE TABLE IF NOT EXISTS superpowers (
  `id`        int          NOT NULL AUTO_INCREMENT   COMMENT 'Superpower id',
  `name`      varchar(128) NOT NULL                  COMMENT 'Superpower name',
  CONSTRAINT superpowers_PK PRIMARY KEY (id),
  CONSTRAINT superpower_name_UN UNIQUE KEY (name)
)
CHARACTER SET utf8 COLLATE utf8_spanish_ci
ENGINE=InnoDB
COMMENT='Special roles to give users super powers'
;
INSERT INTO superpowers (name) VALUES
 ('admin')
;

CREATE TABLE IF NOT EXISTS users (
  `id`        int          NOT NULL AUTO_INCREMENT   COMMENT 'User id',
  `email`     varchar(128) NOT NULL                  COMMENT 'User email address',
  `nickname`  varchar(128) NOT NULL                  COMMENT 'User nick name',
  `fullname`  varchar(128)          DEFAULT nickname COMMENT 'User full name',
  `salt`      varchar(128) NOT NULL                  COMMENT 'Hashing salt',
  `password`  varchar(128) NOT NULL                  COMMENT 'Hashed password',
  `role`      int                                    COMMENT 'Special role of the user. `null` means regular user',
  `timestamp` timestamp    NOT NULL DEFAULT NOW()    COMMENT 'User creation timestamp',
  `verified`  boolean      NOT NULL DEFAULT false    COMMENT 'Email has been verified by system',
  `active`    boolean      NOT NULL DEFAULT true     COMMENT 'User is active',
  CONSTRAINT user_PK PRIMARY KEY (id),
  CONSTRAINT user_email_UN UNIQUE KEY (email),
  CONSTRAINT user_superpowers_FK FOREIGN KEY (role)
    REFERENCES superpowers(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
)
CHARACTER SET utf8 COLLATE utf8_spanish_ci
ENGINE=InnoDB
COMMENT='Users of the platform, identified by an email address'
;
INSERT INTO users (email, salt, password, nickname, role) VALUES
 ('admin@localhost', '$2a$10$IrbIJHf9x/ZvKlVtG4azTO', '$2a$10$IrbIJHf9x/ZvKlVtG4azTOfX2i7uu13wIHuOyh4R6xAZ2jYuqP2he', 'admin', 1)
;


-- ============================================================================================= --
-- USER TABLES: Managed by getemall users
-- ============================================================================================= --