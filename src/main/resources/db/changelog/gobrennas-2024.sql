--liquibase formatted sql

--changeset barneyb:drop-ingredient-display_title
alter table ingredient
    drop display_title;
