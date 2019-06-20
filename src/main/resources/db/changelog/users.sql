--liquibase formatted sql

--changeset barneyb:user-extends-BaseEntity
alter table users
    add _eqkey bigint default extract(epoch from now());
alter table users
    add constraint uk_users__eqkey unique (_eqkey);

alter table users
    add created_at timestamp with time zone not null default now();
alter table users
    add updated_at timestamp with time zone not null default now();

-- noinspection SqlWithoutWhere
update users
set _eqkey = id;

alter table users
    alter _eqkey set not null;
