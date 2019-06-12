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

