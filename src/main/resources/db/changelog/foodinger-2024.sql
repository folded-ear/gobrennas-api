--liquibase formatted sql

--changeset barneyb:app-settings
create table app_setting
(
    id         bigint                   default nextval('public.id_seq')              not null
        constraint pk_app_setting primary key,
    created_at timestamp with time zone default now()                                 not null,
    _eqkey     bigint                   default date_part('epoch', clock_timestamp()) not null
        constraint uk_app_setting__eqkey unique,
    updated_at timestamp with time zone default now()                                 not null,
    name       varchar                                                                not null
        constraint uk_app_setting_name unique,
    type       int                                                                    not null,
    value_str  varchar
);
