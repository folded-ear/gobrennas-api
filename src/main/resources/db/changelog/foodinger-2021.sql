--liquibase formatted sql

--changeset barneyb:embedded-s3file-for-recipe-photos
alter table ingredient
    add photo_type varchar,
    add photo_size bigint;

--changeset barneyb:textract-job-type
create table textract_job (
    id bigserial not null,
    _eqkey bigint not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    owner_id bigint not null,
    object_key varchar not null,
    content_type varchar,
    size bigint,
    ready bool not null default false,
    constraint pk_textract_job primary key (id),
    constraint uk_textract_job__eqkey unique (_eqkey),
    constraint fk_textract_job_owner_id foreign key (owner_id) references users (id) on delete cascade
);

create index textract_job_owner on textract_job (owner_id);

create table textract_job_lines (
    textract_job_id bigint not null,
    x double precision not null,
    y double precision not null,
    width double precision not null,
    height double precision not null,
    text varchar not null,
    constraint fk_textract_job_lines_job_id foreign key (textract_job_id) references textract_job (id) on delete cascade
);

create index textract_job_lines_job on textract_job_lines (textract_job_id);

--changeset barneyb:default-textract-job-eqkey
alter table textract_job
    alter _eqkey set default date_part('epoch'::text, clock_timestamp());

--changeset barneyb:ingredient-uses-base-entity
alter table ingredient
    add _eqkey bigint not null default date_part('epoch'::text, clock_timestamp());

-- noinspection SqlWithoutWhere
update ingredient set
    _eqkey = id;

alter table ingredient
    add constraint uk_ingredient__eqkey unique (_eqkey);

--changeset barneyb:populate-missing-created-and-updated-at
-- Assign ingredients that don't have a create date one based the surrounding
-- dates, assuming ids represent insertion order. It's close enough. Tag the
-- dates with an extra microsecond in case it's useful to go find them later.
with triples as (
    select i.id
         , (
             select r.id
             from ingredient r
             where r.id < i.id
               and r.created_at is not null
             order by r.created_at desc
             limit 1
         ) as prev_id
         , (
             select r.id
             from ingredient r
             where r.id > i.id
               and r.created_at is not null
             order by r.created_at
             limit 1
         ) as next_id
    from ingredient i
    where i.created_at is null
    order by 1
),
pairs as (
    ( -- just before the next dated ingredient
        select i.id
             , r.created_at
                   - make_interval(secs => cast(r.id - i.id as double precision) / 1000) as dt
        from triples i
            join ingredient r on i.next_id = r.id
        where next_id is not null
    ) union ( -- just after the prior dated ingredient
        select i.id
             , r.created_at
                   + make_interval(secs => cast(r.id - i.id as double precision) / 1000) as dt
        from triples i
            join ingredient r on i.prev_id = r.id
        where next_id is null
            and prev_id is not null
    ) union ( -- nothing has a date. :|
        select id, clock_timestamp() as dt
        from triples
        where next_id is null
            and prev_id is null
    )
)
update ingredient set
    created_at = p.dt + make_interval(secs => 0.000001)
from pairs p
where p.id = ingredient.id;

-- Default missing update dates to the ingredient's create date.
update ingredient set
    updated_at = created_at
where updated_at is null;

--changeset barneyb:recipe-photo-focus
alter table ingredient
    add photo_focus_top real,
    add photo_focus_left real;

--changeset barneyb:task-aggregation
alter table task
    add aggregate_id bigint;
alter table task
    add constraint fk_task_aggregate_id foreign key (aggregate_id) references task (id) on delete set null;

--changeset barneyb:task-notes
alter table task
    add notes varchar;
