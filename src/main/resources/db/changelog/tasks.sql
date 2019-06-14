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
create table task_grants
(
    task_id bigint  not null,
    user_id bigint  not null,
    perm    varchar not null,
    constraint pk_task_grants primary key (task_id, user_id),
    constraint fk_task_grants_task foreign key (task_id) references task (id)
        on delete cascade,
    constraint fk_task_grants_user foreign key (user_id) references users (id)
        on delete cascade
);