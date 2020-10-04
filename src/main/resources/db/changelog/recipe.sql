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

--changeset barneyb:no-unparsed-quantities
alter table recipe_ingredients
    drop quantity;

alter table recipe_ingredients
    rename column amount to quantity;

--changeset barneyb:unit-storage
create table unit_of_measure
(
    id          bigserial not null,
    name        varchar,
    plural_name varchar,
    constraint pk_uom primary key (id),
    constraint uk_uom_name unique (name)
);
create table unit_of_measure_aliases
(
    unit_of_measure_id bigint not null,
    alias              varchar,
    constraint pk_uom_aliases primary key (unit_of_measure_id, alias),
    constraint fk_uom_aliases_oum_id foreign key (unit_of_measure_id) references unit_of_measure on delete cascade
);
create index idx_uom_alias on unit_of_measure_aliases (alias);
create table unit_of_measure_conversions
(
    unit_of_measure_id bigint not null,
    target_id          bigint not null,
    factor             double precision,
    constraint pk_uom_conversions primary key (unit_of_measure_id, target_id),
    constraint fk_oum_conversions_oum_id foreign key (target_id) references unit_of_measure on delete cascade ,
    constraint fk_oum_conversions_target_id foreign key (unit_of_measure_id) references unit_of_measure on delete cascade
);

alter table recipe_ingredients
    alter column quantity type double precision;
alter table recipe_ingredients
    add units_id bigint;
alter table recipe_ingredients
    add constraint fk_recipe_ingredients_units_id foreign key (units_id) references unit_of_measure (id) on delete set null;

insert into unit_of_measure
    (name)
select distinct units
from recipe_ingredients ri
where not exists(
        select 1
        from unit_of_measure
        where name = ri.units
    )
order by 1;

update recipe_ingredients
set units_id = (
    select id
    from unit_of_measure
    where name = recipe_ingredients.units
);

alter table recipe_ingredients
    drop column units;

--changeset barneyb:cant-use-longs-in-js
alter table unit_of_measure
    add _eqkey bigint default extract(epoch from now());
alter table unit_of_measure
    add created_at timestamp with time zone not null default now();
alter table unit_of_measure
    add updated_at timestamp with time zone not null default now();

alter table unit_of_measure_aliases
    drop constraint fk_uom_aliases_oum_id;
alter table unit_of_measure_conversions
    drop constraint fk_oum_conversions_oum_id;
alter table unit_of_measure_conversions
    drop constraint fk_oum_conversions_target_id;
alter table recipe_ingredients
    drop constraint fk_recipe_ingredients_units_id;

update unit_of_measure set id = abs(id << 20) >> 20;
update unit_of_measure_aliases set unit_of_measure_id = abs(unit_of_measure_id << 20) >> 20;
update unit_of_measure_conversions set target_id = abs(target_id << 20) >> 20,
                                       unit_of_measure_id = abs(unit_of_measure_id << 20) >> 20;
update recipe_ingredients set units_id = abs(units_id << 20) >> 20;

alter table unit_of_measure_aliases
    add constraint fk_uom_aliases_oum_id foreign key (unit_of_measure_id) references unit_of_measure on delete cascade;
alter table unit_of_measure_conversions
    add constraint fk_oum_conversions_oum_id foreign key (target_id) references unit_of_measure on delete cascade;
alter table unit_of_measure_conversions
    add constraint fk_oum_conversions_target_id foreign key (unit_of_measure_id) references unit_of_measure on delete cascade;
alter table recipe_ingredients
    add constraint fk_recipe_ingredients_units_id foreign key (units_id) references unit_of_measure (id) on delete set null;

--changeset switzerb:yield-calories-time-on-recipe
alter table ingredient
    add yield int;
alter table ingredient
    add calories int;
alter table ingredient
    add total_time int;

--changeset barneyb:pantry-item-store-order
alter table ingredient
    add store_order int null;

with raw as (
    select i.id
         , i.name
         , case when shopping_list_id is null
             then 1
             else percent_rank() over (partition by shopping_list_id order by completed_at)
         end rank
    from ingredient i
        left join shopping_list_items li on li.ingredient_id = i.id
            and li.completed_at is not null
    where i.dtype = 'PantryItem'
),
orders as (
    select r.id
         , row_number() over (order by avg(r.rank), r.name) store_order
    from raw r
    group by r.id, r.name
)
update ingredient set
    store_order = orders.store_order
from orders
where orders.id = ingredient.id;
