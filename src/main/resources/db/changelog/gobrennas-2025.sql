--liquibase formatted sql

--changeset barneyb:fix-app-setting-data-type
UPDATE app_setting
   SET type = 1
 WHERE type = 0;
