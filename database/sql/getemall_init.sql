-- ============================================================================================= --
-- Getemall database initialization script.
--
-- Created by miquifant on 2020-11-14
-- ============================================================================================= --

CREATE DATABASE getemall
  DEFAULT CHARSET=utf8
  COLLATE=utf8_spanish_ci;

USE getemall;

source getemall_privileges.sql
source getemall_schema.sql
