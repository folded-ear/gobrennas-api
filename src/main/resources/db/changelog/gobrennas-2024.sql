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
