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
