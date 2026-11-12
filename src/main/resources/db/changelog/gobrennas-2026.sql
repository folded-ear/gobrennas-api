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

--changeset barneyb:add-position-to-plan-bucket
ALTER TABLE plan_bucket
    ADD COLUMN position INTEGER;
UPDATE plan_bucket b
SET position = r.rn
FROM (SELECT id,
             ROW_NUMBER() OVER (PARTITION BY plan_id ORDER BY id) AS rn
      FROM plan_bucket) r
WHERE b.id = r.id;
ALTER TABLE plan_bucket
    ALTER COLUMN position SET NOT NULL;

--changeset barneyb:add-status-to-textract-job
ALTER TABLE textract_job
    ADD COLUMN status_id BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN error_message VARCHAR,
    ADD COLUMN retryable BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE textract_job
SET status_id = 1
WHERE ready;
UPDATE textract_job
SET status_id     = 2,
    error_message = 'Unknown error.'
WHERE NOT ready;
ALTER TABLE textract_job
    DROP COLUMN ready;

--changeset barneyb:add-ingredient-acquisition-table
CREATE TABLE ingredient_acquisition
(
    id            BIGINT                   NOT NULL DEFAULT NEXTVAL('id_seq'),
    created_at    TIMESTAMP                NOT NULL DEFAULT NOW(),
    _eqkey        BIGINT                   NOT NULL DEFAULT _eqkey(13),
    updated_at    TIMESTAMP                NOT NULL DEFAULT NOW(),
    user_id       BIGINT                   NOT NULL,
    ingredient_id BIGINT                   NOT NULL,
    acquired_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    latitude      DOUBLE PRECISION         NOT NULL,
    longitude     DOUBLE PRECISION         NOT NULL,
    accuracy      DOUBLE PRECISION         NOT NULL,
    CONSTRAINT pk_ingredient_acquisition PRIMARY KEY (id),
    CONSTRAINT fk_ingredient_acquisition_user_id
        FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE,
    CONSTRAINT fk_ingredient_acquisition_ingredient_id
        FOREIGN KEY (ingredient_id) REFERENCES ingredient ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_ingredient_acquisition__eqkey
    ON ingredient_acquisition (_eqkey);
CREATE INDEX idx_ingredient_acquisition_user
    ON ingredient_acquisition (user_id);
CREATE INDEX idx_ingredient_acquisition_ingredient
    ON ingredient_acquisition (ingredient_id);
