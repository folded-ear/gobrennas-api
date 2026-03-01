--liquibase formatted sql

--changeset barneyb:add-can-have-multiple-plans-preference
INSERT INTO preference
    (name, type, default_value_str)
VALUES ('canHaveMultiplePlans', 2, 'true');
