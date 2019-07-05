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