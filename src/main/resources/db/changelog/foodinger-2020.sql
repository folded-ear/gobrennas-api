--liquibase formatted sql

--changeset barneyb:change-plan-discriminators
alter table task
    drop constraint "chk_task_owner_id",
    drop constraint "chk_task_parent_id";

update task set
    _type = 'plan'
where _type = 'list';

update task set
    _type = 'item'
where _type = 'task';

alter table task
    add constraint "chk_task_owner_id" CHECK (
        CASE _type
            WHEN 'plan'::text THEN owner_id IS NOT NULL
            ELSE owner_id IS NULL
        END),
    add constraint "chk_task_parent_id" CHECK (
        CASE _type
            WHEN 'plan'::text THEN parent_id IS NULL
            ELSE parent_id IS NOT NULL
        END);

--changeset barneyb:plan-buckets
create table plan_bucket (
    id bigserial not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    _eqkey bigint not null default date_part('epoch'::text, clock_timestamp()),
    plan_id bigint not null,
    name varchar,
    date date,
    constraint pk_plan_bucket primary key (id),
    constraint uk_plan_bucket__eqkey unique (_eqkey),
    constraint fk_plan_bucket_plan_id foreign key (plan_id) references task (id) on delete cascade
);

alter table task
    add bucket_id bigint null,
    add constraint fk_task_bucket_id foreign key (bucket_id) references plan_bucket (id) on delete set null;
