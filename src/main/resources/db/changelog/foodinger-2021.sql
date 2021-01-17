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
