--liquibase formatted sql

--changeset barneyb:fix-app-setting-data-type
UPDATE app_setting
   SET type = 1
 WHERE type = 0;


--changeset barneyb:_eqkey-function splitStatements:false
CREATE OR REPLACE FUNCTION _eqkey(table_num BIGINT) RETURNS BIGINT AS
$$
BEGIN
    RETURN (DATE_PART('epoch', CLOCK_TIMESTAMP())::BIGINT << 32)
               | ((table_num & x'ff'::BIGINT) << 24)
        | (RANDOM() * x'ffffff'::BIGINT)::BIGINT;
END;
$$ LANGUAGE plpgsql;


--changeset barneyb:use-function-for-eqkey-defaults
ALTER TABLE app_setting
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(1);
ALTER TABLE plan_item
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(2);
ALTER TABLE textract_job
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(3);
ALTER TABLE favorite
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(4);
ALTER TABLE ingredient
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(5);
ALTER TABLE plan_bucket
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(6);
ALTER TABLE users
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(7);
ALTER TABLE unit_of_measure
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(8);
ALTER TABLE planned_recipe_history
    ALTER COLUMN _eqkey SET DEFAULT _eqkey(9);


--changeset barneyb:add-preference-storage
CREATE TABLE preference
(
    id                BIGINT    NOT NULL DEFAULT NEXTVAL('id_seq'),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    _eqkey            BIGINT    NOT NULL DEFAULT _eqkey(10),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    name              VARCHAR   NOT NULL,
    type              INTEGER   NOT NULL,
    default_value_str VARCHAR   NULL,
    CONSTRAINT pk_preference PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_preference__eqkey ON preference (_eqkey);
CREATE UNIQUE INDEX uk_preference_name ON preference (name);


--changeset barneyb:add-user-device-storage
CREATE TABLE user_device
(
    id         BIGINT    NOT NULL DEFAULT NEXTVAL('id_seq'),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    _eqkey     BIGINT    NOT NULL DEFAULT _eqkey(11),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    user_id    BIGINT    NOT NULL,
    device_key VARCHAR   NOT NULL,
    name       VARCHAR   NOT NULL,
    CONSTRAINT pk_user_device PRIMARY KEY (id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_user_device__eqkey ON user_device (_eqkey);
CREATE UNIQUE INDEX uk_user_device_key ON user_device (user_id, device_key);


--changeset barneyb:add-user-preference-storage
CREATE TABLE user_preference
(
    id            BIGINT    NOT NULL DEFAULT NEXTVAL('id_seq'),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    _eqkey        BIGINT    NOT NULL DEFAULT _eqkey(12),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    user_id       BIGINT    NOT NULL,
    device_id     BIGINT    NULL,
    preference_id BIGINT    NOT NULL,
    value_str     VARCHAR   NULL,
    CONSTRAINT pk_user_preference PRIMARY KEY (id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE,
    CONSTRAINT fk_device_id FOREIGN KEY (device_id) REFERENCES user_device ON DELETE CASCADE,
    CONSTRAINT fk_preference_id FOREIGN KEY (preference_id) REFERENCES preference
);

CREATE UNIQUE INDEX uk_user_preference__eqkey ON user_preference (_eqkey);
CREATE UNIQUE INDEX uk_user_preference_user_device_pref ON user_preference (user_id, device_id, preference_id);


--changeset barneyb:set-up-existing-preferences
INSERT INTO preference
    (name, type, default_value_str)
VALUES ('activePlan', 4, NULL)
     , ('activeShoppingPlans', 5, NULL)
     , ('devMode', 2, 'false')
     , ('layout', 3, 'auto')
     , ('navCollapsed', 2, 'false');


--changeset barneyb:track-last-ensured-timestamp-on-devices
ALTER TABLE user_device
    ADD COLUMN last_ensured_at TIMESTAMP NOT NULL DEFAULT NOW();

UPDATE user_device
   SET last_ensured_at = created_at
 WHERE last_ensured_at = NOW();


--changeset barneyb:recipe-sections
ALTER TABLE ingredient
    ADD section_of_id BIGINT NULL;

CREATE INDEX idx_ingredient_section_of ON ingredient (section_of_id);

ALTER TABLE ingredient
    ADD CONSTRAINT fk_ingredient_section_of
        FOREIGN KEY (section_of_id)
            REFERENCES ingredient (id)
            ON DELETE RESTRICT;

ALTER TABLE recipe_ingredients
    ADD is_section BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_ingredient_recipe ON recipe_ingredients (ingredient_id, recipe_id);


--changeset barneyb:ingredient-label-indexes-and-key
ALTER TABLE ingredient_labels
    DROP CONSTRAINT fk_ingredient_id;
CREATE INDEX idx_ingredient_labels_ingredient ON ingredient_labels (ingredient_id, label_id);
CREATE INDEX idx_ingredient_labels_label ON ingredient_labels (label_id, ingredient_id);
ALTER TABLE ingredient_labels
    ADD CONSTRAINT fk_ingredient_id FOREIGN KEY (ingredient_id)
        REFERENCES ingredient (id)
        ON DELETE CASCADE;


--changeset barneyb:plan-item-indexes
CREATE INDEX idx_plan_item_parent ON plan_item (parent_id);
CREATE INDEX idx_plan_item_aggregate ON plan_item (aggregate_id);
CREATE INDEX idx_plan_item_trash_bin ON plan_item (trash_bin_id);
CREATE INDEX idx_plan_item_updated_at ON plan_item (updated_at);
