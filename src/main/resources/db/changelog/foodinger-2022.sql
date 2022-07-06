--liquibase formatted sql

--changeset barneyb:use-a-shared-identity-sequence
create sequence id_seq increment 20;
select setval('id_seq', (
    with raw as (
        (select max(id) id from compound_quantity) union
        (select max(id) id from ingredient) union
        (select max(id) id from inventory_item) union
        (select max(id) id from inventory_tx) union
        (select max(id) id from label) union
        (select max(id) id from plan_bucket) union
        (select max(id) id from task) union
        (select max(id) id from textract_job) union
        (select max(id) id from unit_of_measure) union
        (select max(id) id from users)
    )
    select coalesce(max(id), 0) + 1 from raw
    ));

-- drop all FKs
alter table "compound_quantity_components" drop constraint "fk_compound_quantity_components_compound_quantity_id";
alter table "compound_quantity_components" drop constraint "fk_compound_quantity_components_units_id";
alter table "ingredient" drop constraint "fk_ingredient_owner";
alter table "ingredient_labels" drop constraint "fk_ingredient_id";
alter table "ingredient_labels" drop constraint "fk_label_id";
alter table "inventory_item" drop constraint "fk_inventory_item_pantry_item_id";
alter table "inventory_item" drop constraint "fk_inventory_item_quantity_id";
alter table "inventory_item" drop constraint "fk_inventory_item_user_id";
alter table "inventory_tx" drop constraint "fk_inventory_tx_item_id";
alter table "inventory_tx" drop constraint "fk_inventory_tx_new_quantity_id";
alter table "inventory_tx" drop constraint "fk_inventory_tx_quantity_id";
alter table "plan_bucket" drop constraint "fk_plan_bucket_plan_id";
alter table "recipe_ingredients" drop constraint "fk115hxprua4ai0chlhicwmuhna";
alter table "recipe_ingredients" drop constraint "fk8kr5xdvi2l7pswlri2i70g8av";
alter table "recipe_ingredients" drop constraint "fk_recipe_ingredients_units_id";
alter table "task" drop constraint "fk_task_aggregate_id";
alter table "task" drop constraint "fk_task_bucket_id";
alter table "task" drop constraint "fk_task_ingredients_id";
alter table "task" drop constraint "fk_task_ingredients_units_id";
alter table "task" drop constraint "fk_task_owner";
alter table "task" drop constraint "fk_task_parent";
alter table "task_list_grants" drop constraint "fk_task_list_grants_task";
alter table "task_list_grants" drop constraint "fk_task_list_grants_user";
alter table "textract_job" drop constraint "fk_textract_job_owner_id";
alter table "textract_job_lines" drop constraint "fk_textract_job_lines_job_id";
alter table "unit_of_measure_aliases" drop constraint "fk_uom_aliases_oum_id";
alter table "unit_of_measure_conversions" drop constraint "fk_oum_conversions_oum_id";
alter table "unit_of_measure_conversions" drop constraint "fk_oum_conversions_target_id";

-- recreate w/ cascade update
alter table "compound_quantity_components" add constraint "fk_compound_quantity_components_compound_quantity_id" FOREIGN KEY (compound_quantity_id) REFERENCES compound_quantity(id) on update cascade;
alter table "compound_quantity_components" add constraint "fk_compound_quantity_components_units_id" FOREIGN KEY (units_id) REFERENCES unit_of_measure(id) on update cascade;
alter table "ingredient" add constraint "fk_ingredient_owner" FOREIGN KEY (owner_id) REFERENCES users(id) on update cascade;
alter table "ingredient_labels" add constraint "fk_ingredient_id" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) on update cascade;
alter table "ingredient_labels" add constraint "fk_label_id" FOREIGN KEY (label_id) REFERENCES label(id) on update cascade;
alter table "inventory_item" add constraint "fk_inventory_item_pantry_item_id" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) on update cascade;
alter table "inventory_item" add constraint "fk_inventory_item_quantity_id" FOREIGN KEY (quantity_id) REFERENCES compound_quantity(id) on update cascade;
alter table "inventory_item" add constraint "fk_inventory_item_user_id" FOREIGN KEY (user_id) REFERENCES users(id) on update cascade;
alter table "inventory_tx" add constraint "fk_inventory_tx_item_id" FOREIGN KEY (item_id) REFERENCES inventory_item(id) on update cascade;
alter table "inventory_tx" add constraint "fk_inventory_tx_new_quantity_id" FOREIGN KEY (new_quantity_id) REFERENCES compound_quantity(id) on update cascade;
alter table "inventory_tx" add constraint "fk_inventory_tx_quantity_id" FOREIGN KEY (quantity_id) REFERENCES compound_quantity(id) on update cascade;
alter table "plan_bucket" add constraint "fk_plan_bucket_plan_id" FOREIGN KEY (plan_id) REFERENCES task(id) on update cascade;
alter table "recipe_ingredients" add constraint "fk115hxprua4ai0chlhicwmuhna" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) on update cascade;
alter table "recipe_ingredients" add constraint "fk8kr5xdvi2l7pswlri2i70g8av" FOREIGN KEY (recipe_id) REFERENCES ingredient(id) on update cascade;
alter table "recipe_ingredients" add constraint "fk_recipe_ingredients_units_id" FOREIGN KEY (units_id) REFERENCES unit_of_measure(id) on update cascade;
alter table "task" add constraint "fk_task_aggregate_id" FOREIGN KEY (aggregate_id) REFERENCES task(id) on update cascade;
alter table "task" add constraint "fk_task_bucket_id" FOREIGN KEY (bucket_id) REFERENCES plan_bucket(id) on update cascade;
alter table "task" add constraint "fk_task_ingredients_id" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) on update cascade;
alter table "task" add constraint "fk_task_ingredients_units_id" FOREIGN KEY (units_id) REFERENCES unit_of_measure(id) on update cascade;
alter table "task" add constraint "fk_task_owner" FOREIGN KEY (owner_id) REFERENCES users(id) on update cascade;
alter table "task" add constraint "fk_task_parent" FOREIGN KEY (parent_id) REFERENCES task(id) on update cascade;
alter table "task_list_grants" add constraint "fk_task_list_grants_task" FOREIGN KEY (task_list_id) REFERENCES task(id) on update cascade;
alter table "task_list_grants" add constraint "fk_task_list_grants_user" FOREIGN KEY (user_id) REFERENCES users(id) on update cascade;
alter table "textract_job" add constraint "fk_textract_job_owner_id" FOREIGN KEY (owner_id) REFERENCES users(id) on update cascade;
alter table "textract_job_lines" add constraint "fk_textract_job_lines_job_id" FOREIGN KEY (textract_job_id) REFERENCES textract_job(id) on update cascade;
alter table "unit_of_measure_aliases" add constraint "fk_uom_aliases_oum_id" FOREIGN KEY (unit_of_measure_id) REFERENCES unit_of_measure(id) on update cascade;
alter table "unit_of_measure_conversions" add constraint "fk_oum_conversions_oum_id" FOREIGN KEY (target_id) REFERENCES unit_of_measure(id) on update cascade;
alter table "unit_of_measure_conversions" add constraint "fk_oum_conversions_target_id" FOREIGN KEY (unit_of_measure_id) REFERENCES unit_of_measure(id) on update cascade;

-- rewrite all the surrogate keys
update compound_quantity set id = nextval('id_seq') where id > 0;
update inventory_item set id = nextval('id_seq') where id > 0;
update inventory_tx set id = nextval('id_seq') where id > 0;
update label set id = nextval('id_seq') where id > 0;
update plan_bucket set id = nextval('id_seq') where id > 0;
update textract_job set id = nextval('id_seq') where id > 0;
update unit_of_measure set id = nextval('id_seq') where id > 0;
update users set id = nextval('id_seq') where id > 0;
-- Recipes use their ID for share links, so only reassign the ones that conflict
-- with a task, as tasks' IDs aren't changing.
update ingredient set id = nextval('id_seq') where dtype != 'Recipe';
update ingredient set id = nextval('id_seq') where dtype = 'Recipe' and id in (select id from task);
-- Tasks are directly recursive, which postgres can't deal with in a bulk-update
-- situation. But they're the only one, so just leave their IDs as-is.
-- Everything else will move forward, so any conflicts will be resolved from the
-- other side.
-- update task set id = nextval('id_seq') where id > 0;

-- drop all FKs again
alter table "compound_quantity_components" drop constraint "fk_compound_quantity_components_compound_quantity_id";
alter table "compound_quantity_components" drop constraint "fk_compound_quantity_components_units_id";
alter table "ingredient" drop constraint "fk_ingredient_owner";
alter table "ingredient_labels" drop constraint "fk_ingredient_id";
alter table "ingredient_labels" drop constraint "fk_label_id";
alter table "inventory_item" drop constraint "fk_inventory_item_pantry_item_id";
alter table "inventory_item" drop constraint "fk_inventory_item_quantity_id";
alter table "inventory_item" drop constraint "fk_inventory_item_user_id";
alter table "inventory_tx" drop constraint "fk_inventory_tx_item_id";
alter table "inventory_tx" drop constraint "fk_inventory_tx_new_quantity_id";
alter table "inventory_tx" drop constraint "fk_inventory_tx_quantity_id";
alter table "plan_bucket" drop constraint "fk_plan_bucket_plan_id";
alter table "recipe_ingredients" drop constraint "fk115hxprua4ai0chlhicwmuhna";
alter table "recipe_ingredients" drop constraint "fk8kr5xdvi2l7pswlri2i70g8av";
alter table "recipe_ingredients" drop constraint "fk_recipe_ingredients_units_id";
alter table "task" drop constraint "fk_task_aggregate_id";
alter table "task" drop constraint "fk_task_bucket_id";
alter table "task" drop constraint "fk_task_ingredients_id";
alter table "task" drop constraint "fk_task_ingredients_units_id";
alter table "task" drop constraint "fk_task_owner";
alter table "task" drop constraint "fk_task_parent";
alter table "task_list_grants" drop constraint "fk_task_list_grants_task";
alter table "task_list_grants" drop constraint "fk_task_list_grants_user";
alter table "textract_job" drop constraint "fk_textract_job_owner_id";
alter table "textract_job_lines" drop constraint "fk_textract_job_lines_job_id";
alter table "unit_of_measure_aliases" drop constraint "fk_uom_aliases_oum_id";
alter table "unit_of_measure_conversions" drop constraint "fk_oum_conversions_oum_id";
alter table "unit_of_measure_conversions" drop constraint "fk_oum_conversions_target_id";

-- recreate the way they were before
alter table "compound_quantity_components" add constraint "fk_compound_quantity_components_compound_quantity_id" FOREIGN KEY (compound_quantity_id) REFERENCES compound_quantity(id) ON DELETE CASCADE;
alter table "compound_quantity_components" add constraint "fk_compound_quantity_components_units_id" FOREIGN KEY (units_id) REFERENCES unit_of_measure(id);
alter table "ingredient" add constraint "fk_ingredient_owner" FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
alter table "ingredient_labels" add constraint "fk_ingredient_id" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id);
alter table "ingredient_labels" add constraint "fk_label_id" FOREIGN KEY (label_id) REFERENCES label(id);
alter table "inventory_item" add constraint "fk_inventory_item_pantry_item_id" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id);
alter table "inventory_item" add constraint "fk_inventory_item_quantity_id" FOREIGN KEY (quantity_id) REFERENCES compound_quantity(id);
alter table "inventory_item" add constraint "fk_inventory_item_user_id" FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
alter table "inventory_tx" add constraint "fk_inventory_tx_item_id" FOREIGN KEY (item_id) REFERENCES inventory_item(id) ON DELETE CASCADE;
alter table "inventory_tx" add constraint "fk_inventory_tx_new_quantity_id" FOREIGN KEY (new_quantity_id) REFERENCES compound_quantity(id);
alter table "inventory_tx" add constraint "fk_inventory_tx_quantity_id" FOREIGN KEY (quantity_id) REFERENCES compound_quantity(id);
alter table "plan_bucket" add constraint "fk_plan_bucket_plan_id" FOREIGN KEY (plan_id) REFERENCES task(id) ON DELETE CASCADE;
alter table "recipe_ingredients" add constraint "fk115hxprua4ai0chlhicwmuhna" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id);
alter table "recipe_ingredients" add constraint "fk8kr5xdvi2l7pswlri2i70g8av" FOREIGN KEY (recipe_id) REFERENCES ingredient(id);
alter table "recipe_ingredients" add constraint "fk_recipe_ingredients_units_id" FOREIGN KEY (units_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL;
alter table "task" add constraint "fk_task_aggregate_id" FOREIGN KEY (aggregate_id) REFERENCES task(id) ON DELETE SET NULL;
alter table "task" add constraint "fk_task_bucket_id" FOREIGN KEY (bucket_id) REFERENCES plan_bucket(id) ON DELETE SET NULL;
alter table "task" add constraint "fk_task_ingredients_id" FOREIGN KEY (ingredient_id) REFERENCES ingredient(id);
alter table "task" add constraint "fk_task_ingredients_units_id" FOREIGN KEY (units_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL;
alter table "task" add constraint "fk_task_owner" FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
alter table "task" add constraint "fk_task_parent" FOREIGN KEY (parent_id) REFERENCES task(id) ON DELETE CASCADE;
alter table "task_list_grants" add constraint "fk_task_list_grants_task" FOREIGN KEY (task_list_id) REFERENCES task(id) ON DELETE CASCADE;
alter table "task_list_grants" add constraint "fk_task_list_grants_user" FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
alter table "textract_job" add constraint "fk_textract_job_owner_id" FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
alter table "textract_job_lines" add constraint "fk_textract_job_lines_job_id" FOREIGN KEY (textract_job_id) REFERENCES textract_job(id) ON DELETE CASCADE;
alter table "unit_of_measure_aliases" add constraint "fk_uom_aliases_oum_id" FOREIGN KEY (unit_of_measure_id) REFERENCES unit_of_measure(id) ON DELETE CASCADE;
alter table "unit_of_measure_conversions" add constraint "fk_oum_conversions_oum_id" FOREIGN KEY (target_id) REFERENCES unit_of_measure(id) ON DELETE CASCADE;
alter table "unit_of_measure_conversions" add constraint "fk_oum_conversions_target_id" FOREIGN KEY (unit_of_measure_id) REFERENCES unit_of_measure(id) ON DELETE CASCADE;

-- no dupes
select id, count(*)
    from (
             (select id from compound_quantity) union all
             (select id from ingredient) union all
             (select id from inventory_item) union all
             (select id from inventory_tx) union all
             (select id from label) union all
             (select id from plan_bucket) union all
             (select id from task) union all
             (select id from textract_job) union all
             (select id from unit_of_measure) union all
             (select id from users)
         ) t
group by t.id
having count(*) > 1;

--changeset bboisvert:use-default-allocation-size-for-id_seq
alter sequence id_seq increment 50;
