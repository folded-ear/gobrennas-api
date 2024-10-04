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

--changeset barneyb:q_ingredient_fulltext_handler splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION q_ingredient_fulltext_handler(id BIGINT) RETURNS VOID AS
$$
BEGIN
    UPDATE ingredient
    SET fulltext = SETWEIGHT(TO_TSVECTOR('en', COALESCE(ingredient.name, '')), 'A') ||
                   SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                           (SELECT STRING_AGG(l.name, ' ' ORDER BY l.name)
                            FROM ingredient_labels il
                                     JOIN label l ON l.id = il.label_id
                            WHERE il.ingredient_id = ingredient.id), '')), 'B') ||
                   CASE ingredient.dtype
                       WHEN 'PantryItem' THEN
                           SETWEIGHT(TO_TSVECTOR('en', COALESCE(
                                   (SELECT STRING_AGG(synonym, CHR(10) ORDER BY synonym)
                                    FROM pantry_item_synonyms link
                                    WHERE pantry_item_id = ingredient.id), '')), 'A')
                       WHEN 'Recipe' THEN
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
                                             JOIN pantry_item_synonyms syn ON link.ingredient_id = syn.pantry_item_id
                                    WHERE recipe_id = ingredient.id), '')), 'C') ||
                           SETWEIGHT(TO_TSVECTOR('en', COALESCE(ingredient.directions, '')), 'D')
                       END
    WHERE ingredient.id = q_ingredient_fulltext_handler.id;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:q_ingredient_fulltext_trig splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION q_ingredient_fulltext_trig() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM q_ingredient_fulltext_handler(old_table.id)
    FROM old_table;
    RETURN NULL;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:queue-centric-schema-object-naming
ALTER TABLE ingredient_fulltext_reindex_queue
    RENAME CONSTRAINT pk_ingredient_reindex_queue
        TO pk_q_ingredient_fulltext;
ALTER TABLE ingredient_fulltext_reindex_queue
    RENAME CONSTRAINT fk_ingredient_reindex_queue
        TO fk_q_ingredient_fulltext;
ALTER INDEX idx_ingredient_fulltext_reindex_queue_ts
    RENAME TO idx_q_ingredient_fulltext_ts;
DROP TRIGGER ingredient_fulltext_update_trigger
    ON ingredient_fulltext_reindex_queue;
DROP FUNCTION ingredient_fulltext_update_handler();
DROP FUNCTION ingredient_fulltext_update(BIGINT);

ALTER TABLE ingredient_fulltext_reindex_queue
    RENAME TO q_ingredient_fulltext;

CREATE TRIGGER q_trig_ingredient_fulltext
    AFTER DELETE
    ON q_ingredient_fulltext
    REFERENCING OLD TABLE AS old_table
    FOR EACH ROW
EXECUTE PROCEDURE q_ingredient_fulltext_trig();

--changeset barneyb:drop-old-duplicate-finder-function
DROP FUNCTION rebuild_pantry_item_duplicates;

--changeset barneyb:q_pantry_item_duplicates_handler splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION q_pantry_item_duplicates_handler(id BIGINT) RETURNS VOID AS
$$
BEGIN
    DELETE
    FROM pantry_item_duplicates
    WHERE pantry_item_id = q_pantry_item_duplicates_handler.id;

    -- tight: name/synonyms as phrases
    INSERT INTO pantry_item_duplicates
    SELECT item.id
         , dupe.id
         , FALSE
         , TS_RANK(dupe.fulltext, q_name) + COALESCE(TS_RANK(dupe.fulltext, q_syn), 0) rank
    FROM ingredient item,
         websearch_to_tsquery('en', '"' || REPLACE(item.name, '"', ' ') || '"') q_name,
         websearch_to_tsquery('en', (SELECT STRING_AGG('"' || REPLACE(synonym, '"', ' ') || '"', ' OR ')
                                     FROM pantry_item_synonyms syn
                                     WHERE pantry_item_id = item.id)) q_syn,
         ingredient dupe
    WHERE item.id = q_pantry_item_duplicates_handler.id
      AND dupe.dtype = 'PantryItem'
      AND dupe.id != item.id
      AND dupe.fulltext @@ CASE
                               WHEN q_syn IS NULL
                                   THEN q_name
                               ELSE q_name || q_syn
        END;

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
    WHERE item.id = q_pantry_item_duplicates_handler.id
      AND dupe.dtype = 'PantryItem'
      AND dupe.id != item.id
      AND dupe.fulltext @@ CASE
                               WHEN q_syn IS NULL
                                   THEN q_name
                               ELSE q_name || q_syn
        END
    ON CONFLICT DO NOTHING;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:q_pantry_item_duplicates_trig splitStatements:false runOnChange:true
CREATE OR REPLACE FUNCTION q_pantry_item_duplicates_trig() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM q_pantry_item_duplicates_handler(old_table.id)
    FROM old_table;
    RETURN NULL;
END
$$ LANGUAGE plpgsql;

--changeset barneyb:pantry-item-duplicates-queue
CREATE TABLE q_pantry_item_duplicates
(
    id BIGINT                   NOT NULL,
    ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CLOCK_TIMESTAMP(),
    CONSTRAINT pk_q_pantry_item_duplicates
        PRIMARY KEY (id),
    CONSTRAINT fk_q_pantry_item_duplicates
        FOREIGN KEY (id)
            REFERENCES ingredient (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_q_pantry_item_duplicates
    ON q_pantry_item_duplicates (ts);

CREATE TRIGGER q_trig_pantry_item_duplicates
    AFTER DELETE
    ON q_pantry_item_duplicates
    REFERENCING OLD TABLE AS old_table
    FOR EACH ROW
EXECUTE PROCEDURE q_pantry_item_duplicates_trig();

INSERT INTO q_pantry_item_duplicates (id)
SELECT id
FROM ingredient
WHERE dtype = 'PantryItem'
ON CONFLICT DO NOTHING;

--changeset barneyb:use-dtype-for-plan-items-discriminator
alter table plan_item
    rename column _type to dtype;

--changeset barneyb:planned-recipe-history
create table planned_recipe_history
(
    id           bigint                   not null default nextval('id_seq'),
    _eqkey       bigint                   not null default date_part('epoch'::text, clock_timestamp()),
    created_at   timestamp with time zone not null,
    updated_at   timestamp with time zone not null,
    recipe_id    bigint                   not null,
    plan_item_id bigint                   not null,
    planned_at   timestamp with time zone not null,
    status_id    bigint                   not null,
    constraint pk_planned_recipe_history primary key (id),
    constraint fk_planned_recipe_history_recipe
        foreign key (recipe_id)
            references ingredient (id)
            on delete cascade
);

create index idx_planned_recipe_history_recipe
    on planned_recipe_history (recipe_id);

--changeset barneyb:expand-planned-recipe-history
alter table planned_recipe_history
    add owner_id bigint,
    add done_at  timestamp with time zone,
    add rating   bigint,
    add notes    text,
    add constraint chk_planned_recipe_history_rating check (rating is null or rating between 1 and 5),
    add constraint fk_planned_recipe_history_owner foreign key (owner_id) references users (id);

update planned_recipe_history h
set owner_id = coalesce((select owner_id
                         from plan_item
                         where id = h.plan_item_id),
                        r.owner_id),
    done_at  = h.created_at
from ingredient r
where r.id = h.recipe_id;

alter table planned_recipe_history
    alter owner_id set not null,
    alter done_at set not null;

--changeset barneyb:remove-timers
drop table timer_grants;
drop table timer;

--changeset barneyb:remove-inventory
drop table inventory_tx;
drop table inventory_item;

--changeset barneyb:remove-compound-quantity
drop table compound_quantity_components;
drop table compound_quantity;

--changeset barneyb:remove--on-stage-label
delete
from label
where name = '--on-stage'

--changeset barneyb:trim-ingredient-names
update ingredient
set name = btrim(name)
where name like ' %'
   or name like '% ';

update pantry_item_synonyms
set synonym = btrim(synonym)
where synonym like ' %'
   or synonym like '% ';

delete
from pantry_item_synonyms s
where exists (select *
              from ingredient
              where id = s.pantry_item_id
                and name = s.synonym);

--changeset barneyb:index-cook-history-owner-status
CREATE INDEX idx_planned_recipe_history_owner_status
    ON planned_recipe_history (owner_id, status_id);

--changeset barneyb:plan-color
ALTER TABLE plan_item
    ADD color VARCHAR;
