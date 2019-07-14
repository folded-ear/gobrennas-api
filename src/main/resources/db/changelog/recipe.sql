--liquibase formatted sql

--changeset barneyb:add-raw-ingredients-to-recipe
alter table ingredient
    add raw_ingredients text;

update ingredient
set raw_ingredients = (
    select string_agg(
        coalesce(quantity, '')
            || ' '
            || (select name
                from ingredient
                where id = ri.ingredient_id)
            || coalesce(preparation, '')
        , chr(10) order by ingredient_id)
    from recipe_ingredients ri
    where recipe_id = ingredient.id
)
where dtype = 'Recipe';

--changeset barneyb:put-missing-space-in-raw-ingredients
update ingredient
set raw_ingredients = (
    select string_agg(
        coalesce(quantity, '')
            || ' '
            || (select name
                from ingredient
                where id = ri.ingredient_id)
            || ' '
            || coalesce(preparation, '')
        , chr(10) order by ingredient_id)
    from recipe_ingredients ri
    where recipe_id = ingredient.id
)
where dtype = 'Recipe';

--changeset switzerb:change-title-to-display_title
alter table ingredient
    rename column title to display_title;

--changeset switzerb:copy-title-to-name-for-recipe
update ingredient
set name = ingredient.display_title
where dtype = 'Recipe';

--changeset barneyb:dont-dual-store-identical-name-and-title
update ingredient
set display_title = null
where dtype = 'Recipe'
    and name = display_title;

--changeset barneyb:recipe-ownership
alter table ingredient
    add owner_id bigint;

alter table ingredient
    add constraint fk_ingredient_owner
        foreign key (owner_id) references users (id) on delete cascade ;

update ingredient
set owner_id = (select min(id) from users)
where dtype = 'Recipe';

alter table ingredient
    add constraint chk_ingredient_owner_id check (
        case dtype
            when 'Recipe' then owner_id is not null
            else owner_id is null
            end
        );

--changeset barneyb:raw-ingredient-refs
delete from recipe_ingredients; -- they were all converted to rawIngredients

alter table recipe_ingredients
    add raw varchar;

--changeset barneyb:convert-raw-ingredients dbms:postgresql
--preconditions onFail:MARK_RAN
with raw as (
    select id
         , trim(unnest(string_to_array(raw_ingredients, chr(10)))) raw
    from ingredient
    where dtype = 'Recipe'
    )
insert into recipe_ingredients
    (recipe_id, raw)
select id, raw
from raw
where length(raw) > 0;

--changeset barneyb:kill-old-raw-ingredients
alter table ingredient
    drop raw_ingredients;

--changeset barneyb:separate-quantity-and-units
alter table recipe_ingredients
    add column units varchar;

alter table recipe_ingredients
    alter column quantity type varchar;

alter table recipe_ingredients
    alter column preparation type varchar;

--changeset barneyb:explicit-ingredient-ref-order
alter table recipe_ingredients
    -- this places non-JPA records last
    add _order int not null default power(2, 30);

create sequence temp_seq;
update recipe_ingredients
set _order = nextval('temp_seq');
drop sequence temp_seq;

--changeset barneyb:amount-on-ingredient-ref
alter table recipe_ingredients
    add amount real;
