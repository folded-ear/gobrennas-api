--liquibase formatted sql

--changeset barneyb:add-ownership
alter table task
    add owner_id bigint;

alter table task
    add constraint fk_task_owner
        foreign key (owner_id) references users (id) on delete cascade;

update task
set owner_id = coalesce(
        (select max(id)
         from users u
         where u.name like '% %'
           and task.name like
               substr(u.name, 1, position(' ' in u.name) - 1) || '%'),
        (select max(id) from users))
where parent_id is null;

-- super kludge, because we know there aren't tasks more than two layers deep
update task
set owner_id = (select owner_id from task p where p.id = task.parent_id)
where owner_id is null;

alter table task
    alter owner_id set not null;

--changeset barneyb:tasks-dont-need-quantity
alter table task
    drop quantity;

--changeset barneyb:additional-user-grants
create table task_list_grants
(
    task_list_id bigint  not null,
    user_id      bigint  not null,
    level        varchar not null,
    constraint pk_task_list_grants primary key (task_list_id, user_id),
    constraint fk_task_list_grants_task foreign key (task_list_id) references task (id)
        on delete cascade,
    constraint fk_task_list_grants_user foreign key (user_id) references users (id)
        on delete cascade
);

--changeset barneyb:task-lists-are-a-separate-type
alter table task
    add _type varchar;

-- noinspection SqlWithoutWhere
update task
set _type = case parent_id when null then 'list' else 'task' end;

alter table task
    alter _type set not null;

alter table task
    alter owner_id drop not null;

alter table task
    add constraint chk_owner_on_list check ( _type != 'list' or owner_id is not null );

--changeset barneyb:_eqkey-storage
alter table task
    add _eqkey bigint default extract(epoch from now());

-- noinspection SqlWithoutWhere
update task
set _eqkey = id;

alter table task
    alter _eqkey set not null;
alter table task
    add constraint uk_task__eqkey unique (_eqkey);

--changeset barneyb:update-existing-task-list-types-correctly

/*
 when i did it originally (in 'task-lists-are-a-separate-type' above), i didn't
 account for "equality of null", and thus made the non-parented records into
 tasks (not lists). oops.
 */

update task
set _type = 'list'
where parent_id is null;

update task
set owner_id = null
where _type != 'list';

alter table task
    drop constraint chk_owner_on_list;
alter table task
    add constraint chk_owner_id check (
        case _type
            when 'list' then owner_id is not null
            else owner_id is null
            end
        );
alter table task
    add constraint chk_parent_id check (
        case _type
            when 'list' then parent_id is null
            else parent_id is not null
            end
        );

--changeset barneyb:constraint-names-are-database-wide
alter table task
    rename constraint chk_owner_id to chk_task_owner_id;

alter table task
    rename constraint chk_parent_id to chk_task_parent_id;

--changeset barneyb:task-provenance
alter table task
    add prov_id bigint;
alter table task
    add prov_type varchar;

--changeset barneyb:task-provenance-was-a-bad-idea
alter table task
    drop prov_id;
alter table task
    drop prov_type;

--changeset barneyb:shopping-lists
create table shopping_list
(
    id         bigserial                not null,
    _eqkey     bigint                   not null default extract(epoch from now()),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    constraint pk_shopping_list primary key (id)
);

create table shopping_list_items
(
    shopping_list_id bigint not null,
    quantity         character varying(255),
    ingredient_id    bigint,
    task_id          bigint,
    completed_at     timestamp without time zone,

    constraint pk_shopping_list_item
        primary key (shopping_list_id, ingredient_id),
    constraint fk_shopping_list_item_ingredient
        foreign key (ingredient_id) references ingredient (id)
            on delete cascade,
    constraint fk_shopping_list_item_task
        foreign key (task_id) references task (id)
            on delete set null,
    constraint fk_shopping_list_item_list
        foreign key (shopping_list_id) references shopping_list (id)
            on delete cascade
);

--changeset barneyb:separate-quantity-and-units
alter table shopping_list_items
    add column units varchar;

--changeset barneyb:amount-on-shopping-list-item
alter table shopping_list_items
    add amount real;

--changeset barneyb:clean-up-shoppinglist-item-storage
alter table shopping_list_items
    drop units;
alter table shopping_list_items
    drop amount;
alter table shopping_list_items
    alter quantity type varchar;

--changeset switzerb:task-implements-item
alter table task
    add column quantity double precision;

alter table task
    add column units_id bigint;

alter table task
    add column ingredient_id bigint;

alter table task
    add column preparation varchar;

alter table task
    add constraint fk_task_ingredients_id
        foreign key (ingredient_id) references ingredient (id);

alter table task
    add constraint fk_task_ingredients_units_id
        foreign key (units_id) references unit_of_measure (id)
            on delete set null;
