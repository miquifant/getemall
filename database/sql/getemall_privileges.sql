-- ============================================================================================= --
-- USERS and PRIVILEGES for getemall database.
--
-- Created by miquifant on 2020-11-09
-- ============================================================================================= --

-- owner user
CREATE USER 'geaowner'@'%' IDENTIFIED BY 'geaownerpassword';
GRANT ALL ON getemall.* TO 'geaowner'@'%';

-- write user
CREATE USER 'geawrite'@'%' IDENTIFIED BY 'geawritepassword';
GRANT SELECT, INSERT, UPDATE, DELETE ON getemall.* TO 'geawrite'@'%';

-- read user
CREATE USER 'gearead'@'%' IDENTIFIED BY 'geareadpassword';
GRANT SELECT ON getemall.* TO 'gearead'@'%';

FLUSH PRIVILEGES;
