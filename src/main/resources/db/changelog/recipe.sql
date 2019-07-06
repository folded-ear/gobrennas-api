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
