-- Création de la table des categories
CREATE TABLE T_CATEGORY(
  C_ID BIGINT NOT NULL,
  C_CODE VARCHAR(100) NOT NULL,
  C_LIBELLE VARCHAR(100) NOT NULL,
  PRIMARY KEY(C_ID),
  CONSTRAINT C_CATEGORY_CODE UNIQUE(C_CODE)
);

-- Commentaires sur la table categories et ses colonnes
COMMENT ON TABLE T_CATEGORY IS 'Table des categories';
COMMENT ON COLUMN T_CATEGORY.C_ID IS 'ID d''une categorie';
COMMENT ON COLUMN T_CATEGORY.C_CODE IS 'Code categorie';
COMMENT ON COLUMN T_CATEGORY.C_LIBELLE IS 'Libelle categorie';


-- Création d'une sequence pour gérer les identifiants techniques des utilisateurs
CREATE SEQUENCE CATEGORY_SEQ
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;