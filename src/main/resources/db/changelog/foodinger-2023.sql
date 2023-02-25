--liquibase formatted sql

--changeset barneyb:recipe-fulltext-index
CREATE EXTENSION unaccent;
CREATE EXTENSION btree_gin;

CREATE TEXT SEARCH CONFIGURATION en (COPY = english);
ALTER TEXT SEARCH CONFIGURATION en
    ALTER MAPPING FOR hword, hword_part, word
        WITH unaccent, english_stem;

ALTER TABLE ingredient
    ADD recipe_fulltext tsvector NULL;

CREATE INDEX idx_recipe_fulltext ON ingredient
    USING GIN (recipe_fulltext, owner_id)
    WHERE dtype = 'Recipe';

--changeset barneyb:recipe-fulltext-update-function splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION recipe_fulltext_update(id BIGINT) RETURNS VOID AS
$$
BEGIN
    UPDATE ingredient
    SET recipe_fulltext = SETWEIGHT(TO_TSVECTOR('en', COALESCE(ingredient.name, '')), 'A') ||
                          SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                                  (SELECT STRING_AGG(l.name, ' ' ORDER BY l.name)
                                   FROM ingredient_labels il
                                        JOIN label l ON l.id = il.label_id
                                   WHERE il.ingredient_id = ingredient.id), '')), 'B') ||
                          SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                                  (SELECT STRING_AGG(
                                                  CASE
                                                      WHEN i.id IS NULL THEN raw
                                                      ELSE COALESCE(i.name, '') || ' ' || COALESCE(preparation, '')
                                                      END,
                                                  CHR(10) ORDER BY _order)
                                   FROM recipe_ingredients link
                                        LEFT JOIN ingredient i ON link.ingredient_id = i.id
                                   WHERE recipe_id = ingredient.id), '')), 'C') ||
                          SETWEIGHT(TO_TSVECTOR('en', COALESCE(ingredient.directions, '')), 'D')
    WHERE ingredient.id = recipe_fulltext_update.id
      AND ingredient.dtype = 'Recipe';
END
$$ LANGUAGE plpgsql;

--changeset barneyb:recipe-fulltext-update-handler splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION recipe_fulltext_update_handler() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM recipe_fulltext_update(old_table.id)
    FROM old_table;
    RETURN NULL;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:recipe-fulltext-reindex-queue
CREATE TABLE recipe_fulltext_reindex_queue
(
    id BIGINT      NOT NULL,
    ts timestamptz NOT NULL DEFAULT CLOCK_TIMESTAMP(),
    CONSTRAINT pk_recipe_reindex_queue
        PRIMARY KEY (id),
    CONSTRAINT fk_recipe_reindex_queue
        FOREIGN KEY (id)
            REFERENCES ingredient (id)
            ON DELETE CASCADE
);

CREATE TRIGGER recipe_fulltext_update_trigger
    AFTER DELETE
    ON recipe_fulltext_reindex_queue
    REFERENCING OLD TABLE AS old_table
    FOR EACH ROW
EXECUTE PROCEDURE recipe_fulltext_update_handler();

--changeset barneyb:recipe-fulltext-reindex-enqueue-everything
INSERT INTO recipe_fulltext_reindex_queue (id)
SELECT id
FROM ingredient
WHERE dtype = 'Recipe'
ON CONFLICT DO NOTHING;

--changeset barneyb:recipe-fulltext-reindex-flush-queue
DELETE
FROM recipe_fulltext_reindex_queue;
