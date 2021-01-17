--liquibase formatted sql

--changeset barneyb:embedded-s3file-for-recipe-photos
alter table ingredient
    add photo_type varchar,
    add photo_size bigint;
