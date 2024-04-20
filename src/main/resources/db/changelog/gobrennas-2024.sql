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
