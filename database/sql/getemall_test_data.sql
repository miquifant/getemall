-- ============================================================================================= --
-- TEST data.
--
-- Created by miquifant on 2020-11-09
-- ============================================================================================= --

USE getemall;

INSERT INTO users (email, salt, password, nickname, role) VALUES
 ('miqui@personal.fake', '$2a$10$h.dl5J86rGH7I8bD9bZeZe', '$2a$10$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwFTGujlnEaZXPf/q7vM5wO', 'miqui',  1)
,('esther@company.fake', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1HlCS4bZJ18JuywdEMLT83E1KDmUhCy', 'esther', null)
,('ramon@example.org',   '$2a$10$E3DgchtVry3qlYlzJCsyxe', '$2a$10$E3DgchtVry3qlYlzJCsyxeSK0fftK4v0ynetVCuDdxGVl1obL.ln2', 'ramon',  null)
;

DELETE FROM organizations
;
INSERT INTO organizations (name, owner) VALUES
 ('Miqui Family', 2),
 ('Fake Company', 3)
;
