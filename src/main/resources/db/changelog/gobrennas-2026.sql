--liquibase formatted sql

--changeset barneyb:add-can-have-multiple-plans-preference
INSERT INTO preference
    (name, type, default_value_str)
VALUES ('canHaveMultiplePlans', 2, 'true');

--changeset barneyb:add-assignee-to-plan-item
ALTER TABLE plan_item
    ADD COLUMN assignee_id BIGINT,
    ADD CONSTRAINT fk_plan_item_assignee_id
        FOREIGN KEY (assignee_id) REFERENCES users (id)
        ON DELETE SET NULL,
    ADD CONSTRAINT chk_plan_item_assignee_id CHECK (
        CASE dtype
            WHEN 'plan'::text THEN assignee_id IS NULL
            ELSE TRUE
        END);

--changeset barneyb:add-planner-plans-preference
INSERT INTO preference
    (name, type, default_value_str)
VALUES ('plannerPlans', 5, NULL);
