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
