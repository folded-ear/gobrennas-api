--liquibase formatted sql

--changeset barneyb:drop-ingredient-display_title
alter table ingredient
    drop display_title;

--changeset barneyb:pantry-item-synonyms
create table pantry_item_synonyms
(
    pantry_item_id bigint  not null,
    synonym        varchar not null,
    constraint pk_pantry_item_synonyms
        primary key (pantry_item_id, synonym)
);

create index idx_pantry_item_synonym
    on pantry_item_synonyms
        (synonym, pantry_item_id);

--changeset barneyb:drop-ingredient-aisle
alter table ingredient
    drop aisle;

--changeset barneyb:tear-down-recipe-fulltext-indexing
drop trigger recipe_fulltext_update_trigger
    on recipe_fulltext_reindex_queue;
drop index idx_recipe_fulltext;
alter table ingredient
    rename recipe_fulltext to fulltext;
drop function recipe_fulltext_update_handler();
drop function recipe_fulltext_update(BIGINT);

--changeset barneyb:convert-recipe-fulltext-to-ingredient
alter table recipe_fulltext_reindex_queue
    rename constraint fk_recipe_reindex_queue to fk_ingredient_reindex_queue;
alter table recipe_fulltext_reindex_queue
    rename constraint pk_recipe_reindex_queue to pk_ingredient_reindex_queue;
alter table recipe_fulltext_reindex_queue
    rename to ingredient_fulltext_reindex_queue;
alter index idx_recipe_fulltext_reindex_queue_ts
    rename to idx_ingredient_fulltext_reindex_queue_ts;

insert into ingredient_fulltext_reindex_queue
select id
from ingredient
on conflict do nothing;

--changeset barneyb:ingredient_fulltext_update splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION ingredient_fulltext_update(id BIGINT) RETURNS VOID AS
$$
BEGIN
    UPDATE ingredient
    SET fulltext = SETWEIGHT(TO_TSVECTOR('en', COALESCE(ingredient.name, '')), 'A') ||
                   SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                           (SELECT STRING_AGG(l.name, ' ' ORDER BY l.name)
                            FROM ingredient_labels il
                                     JOIN label l ON l.id = il.label_id
                            WHERE il.ingredient_id = ingredient.id), '')), 'B') ||
                   case ingredient.dtype
                       when 'PantryItem' then
                           SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                                   (SELECT STRING_AGG(synonym, CHR(10) ORDER BY synonym)
                                    FROM pantry_item_synonyms link
                                    WHERE pantry_item_id = ingredient.id), '')), 'A')
                       when 'Recipe' then
                           SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                                   (SELECT STRING_AGG(
                                                   CASE
                                                       WHEN i.id IS NULL THEN COALESCE(raw, '')
                                                       ELSE COALESCE(i.name, '') || ' ' || COALESCE(raw, '')
                                                       END,
                                                   CHR(10) ORDER BY _order)
                                    FROM recipe_ingredients link
                                             LEFT JOIN ingredient i ON link.ingredient_id = i.id
                                    WHERE recipe_id = ingredient.id), '')), 'C') ||
                           SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                                   (SELECT STRING_AGG(DISTINCT synonym, CHR(10))
                                    FROM recipe_ingredients link
                                             JOIN pantry_item_synonyms syn on link.ingredient_id = syn.pantry_item_id
                                    WHERE recipe_id = ingredient.id), '')), 'C') ||
                           SETWEIGHT(TO_TSVECTOR('en', COALESCE(ingredient.directions, '')), 'D')
                       end
    WHERE ingredient.id = ingredient_fulltext_update.id;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:ingredient_fulltext_triggger_function splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION ingredient_fulltext_update_handler() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM ingredient_fulltext_update(old_table.id)
    FROM old_table;
    RETURN NULL;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:ingredient_fulltext_update_trigger
CREATE TRIGGER ingredient_fulltext_update_trigger
    AFTER DELETE
    ON ingredient_fulltext_reindex_queue
    REFERENCING OLD TABLE AS old_table
    FOR EACH ROW
EXECUTE PROCEDURE ingredient_fulltext_update_handler();

--changeset barneyb:ingredient_fulltext_index
CREATE INDEX idx_ingredient_fulltext ON ingredient
    USING GIN (fulltext, dtype, owner_id);

--changeset barneyb:index-recipe-ingredients
CREATE INDEX idx_recipe_ingredient ON recipe_ingredients
    (recipe_id, ingredient_id);

--changeset barneyb:pantry-item-duplicate-storage
CREATE TABLE pantry_item_duplicates
(
    pantry_item_id BIGINT  NOT NULL
        CONSTRAINT fk_pantry_item_duplicates_item
            REFERENCES ingredient
            ON DELETE CASCADE,
    duplicate_id   BIGINT  NOT NULL
        CONSTRAINT fk_pantry_item_duplicates_duplicate
            REFERENCES ingredient
            ON DELETE CASCADE,
    loose          BOOLEAN NOT NULL,
    match_rank     REAL    NOT NULL,
    CONSTRAINT pk_pantry_item_duplicates
        PRIMARY KEY (pantry_item_id, duplicate_id)
);

CREATE INDEX idx_pantry_item_duplicates
    ON pantry_item_duplicates
        (pantry_item_id, duplicate_id, loose, match_rank);

--changeset barneyb:rebuild_pantry_item_duplicates splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION rebuild_pantry_item_duplicates() RETURNS BIGINT AS
$$
DECLARE
    tight_count BIGINT;
    loose_count BIGINT;
BEGIN
    -- noinspection SqlWithoutWhere
    DELETE -- truncate is faster, but non-transactional
    FROM pantry_item_duplicates;

    -- tight: name/synonyms as phrases
    INSERT INTO pantry_item_duplicates
    SELECT item.id
         , dupe.id
         , FALSE
         , TS_RANK(dupe.fulltext, q_name) + COALESCE(TS_RANK(dupe.fulltext, q_syn), 0) rank
    FROM ingredient item,
         websearch_to_tsquery('en', '"' || REPLACE(item.name, '-', ' ') || '"') q_name,
         websearch_to_tsquery('en',
                              (SELECT REPLACE('"' || STRING_AGG(REPLACE(synonym, '-', ' '), ' ') || '"', ' ', ' OR ')
                               FROM pantry_item_synonyms syn
                               WHERE pantry_item_id = item.id)) q_syn,
         ingredient dupe
    WHERE item.dtype = 'PantryItem'
      AND dupe.dtype = 'PantryItem'
      AND dupe.id != item.id
      AND (dupe.fulltext @@ q_name
        OR (q_syn IS NOT NULL AND dupe.fulltext @@ q_syn));

    GET DIAGNOSTICS tight_count = ROW_COUNT;

    -- loose: each separate word
    INSERT INTO pantry_item_duplicates
    SELECT item.id
         , dupe.id
         , TRUE
         , TS_RANK(dupe.fulltext, q_name) + COALESCE(TS_RANK(dupe.fulltext, q_syn), 0) rank
    FROM ingredient item,
         websearch_to_tsquery('en', REPLACE(REPLACE(item.name, '-', ' '), ' ', ' OR ')) q_name,
         websearch_to_tsquery('en', (SELECT REPLACE(STRING_AGG(REPLACE(synonym, '-', ' '), ' '), ' ', ' OR ')
                                     FROM pantry_item_synonyms syn
                                     WHERE pantry_item_id = item.id)) q_syn,
         ingredient dupe
    WHERE item.dtype = 'PantryItem'
      AND dupe.dtype = 'PantryItem'
      AND dupe.id != item.id
      AND (dupe.fulltext @@ q_name
        OR (q_syn IS NOT NULL AND dupe.fulltext @@ q_syn))
    ON CONFLICT DO NOTHING;

    GET DIAGNOSTICS loose_count = ROW_COUNT;

    RETURN tight_count + loose_count;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:index-pantry-items-by-updated_at
CREATE INDEX idx_pantry_item_updated_at
    ON ingredient (updated_at)
    WHERE dtype = 'PantryItem';

--changeset barneyb:index-plan-items-by-ingredient
CREATE INDEX idx_plan_item_ingredient
    ON plan_item (ingredient_id);

--changeset barneyb:index-recipes-and-pantry-items-separately
DROP INDEX idx_ingredient_fulltext;

CREATE INDEX idx_recipe_fulltext ON ingredient
    USING GIN (fulltext, owner_id)
    WHERE dtype = 'Recipe';

CREATE INDEX idx_pantry_item_fulltext ON ingredient
    USING GIN (fulltext, owner_id)
    WHERE dtype = 'PantryItem';
