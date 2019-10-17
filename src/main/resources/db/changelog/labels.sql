--liquibase formatted sql

--changeset switzerb:add-labels-to-ingredient
create table label (
    id bigserial not null,
    name varchar not null,
    constraint pk_label primary key (id),
    constraint uk_label_name unique (name)
);
create index idx_label_name on label (name);

create table ingredient_labels (
    ingredient_id bigint not null,
    label_id bigint not null,
    constraint fk_label_id foreign key (label_id) references label,
    constraint fk_ingredient_id foreign key (ingredient_id) references ingredient
)
