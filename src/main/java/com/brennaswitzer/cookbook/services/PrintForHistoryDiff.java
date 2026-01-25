package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.ValueUtils;
import jakarta.annotation.Nonnull;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * I print a library and planned recipe pair for building a history diff. If the
 * planned recipe has "extra" stuff under it, those item(s) are sunk into the
 * provided consumer for the caller to do something with.
 */
class PrintForHistoryDiff {

    record Lines(List<String> recipe,
                 List<String> planned) {}

    private final PrintRecipe recipe;
    private final PrintPlanned planned;

    PrintForHistoryDiff(Recipe recipe, PlanItem planned) {
        this.recipe = new PrintRecipe(recipe);
        this.planned = new PrintPlanned(planned);
    }

    PrintForHistoryDiff withRecipeScale(double scale) {
        recipe.setScale(scale);
        return this;
    }

    PrintForHistoryDiff withExtraPlanItemSink(@Nonnull Consumer<PlanItem> sink) {
        planned.setPlanItemSink(sink);
        return this;
    }

    Lines print() {
        return new Lines(recipe.getLines(),
                         planned.getLines(recipe.getSections()));
    }

    private abstract static class Print<T> {

        final Map<Long, T> sections = new LinkedHashMap<>();
        private List<String> lines;

        final List<String> getLines() {
            lines = new ArrayList<>();
            buildLines();
            return lines;
        }

        final void print(String line) {
            lines.add(line);
        }

        abstract void buildLines();

        void addSectionDivider(String name) {
            print("");
            print(String.format(
                    "-- %s --%s",
                    name,
                    "-".repeat(Math.max(0, 64 - name.length()))));
        }

    }

    private static class PrintRecipe extends Print<Recipe> {

        private final Recipe recipe;
        @Setter
        private double scale = 1;

        private PrintRecipe(Recipe recipe) {
            this.recipe = recipe;
        }

        Map<Long, Recipe> getSections() {
            return sections;
        }

        @Override
        void buildLines() {
            print(recipe.getName());
            print("");
            addIngredients(recipe);

            // now add all the sections
            for (var s : sections.values()) {
                addSectionDivider(s.getName());
                addIngredients(s);
            }
        }

        private void addIngredients(Recipe r) {
            for (var ir : r.getIngredients()) {
                print(ir.scale(scale).toRaw(true));
                if (ir.isSection()
                    && Hibernate.unproxy(ir.getIngredient()) instanceof Recipe s) {
                    sections.put(s.getId(), s);
                }
            }
            if (ValueUtils.hasValue(r.getDirections())) {
                print("");
                r.getDirections()
                        .lines()
                        .forEach(this::print);
            }
        }

    }

    private static class PrintPlanned extends Print<PlanItem> {

        private final PlanItem planned;
        @Setter
        private Consumer<PlanItem> planItemSink = it -> {};

        private Map<Long, Recipe> recipeSections;

        private PrintPlanned(PlanItem planned) {
            this.planned = planned;
        }

        List<String> getLines(Map<Long, Recipe> recipeSections) {
            this.recipeSections = recipeSections;
            return getLines();
        }

        @Override
        void buildLines() {
            print(planned.getName());
            print("");
            addIngredients(planned);

            // add shared sections, in recipe order
            for (var id : recipeSections.keySet()) {
                var s = sections.get(id);
                if (s == null) continue;
                addSectionDivider(s.getName());
                addIngredients(s);
            }

            // add any plan-only sections
            sections.forEach((id, s) -> {
                if (!recipeSections.containsKey(id)) {
                    addSectionDivider(s.getName());
                    addIngredients(s);
                }
            });
        }

        private void addIngredients(PlanItem item) {
            for (var it : item.getOrderedChildView()) {
                print(it.toRaw(true));
                if (it.hasIngredient()
                    && Hibernate.unproxy(it.getIngredient()) instanceof Recipe r
                    && (recipeSections.containsKey(r.getId())
                        || r.isOwnedSection())) {
                    sections.put(r.getId(), it);
                } else {
                    // non-section recipe or anonymous item
                    planItemSink.accept(it);
                }
            }
            // Plan item notes are copy-on-write, so use the recipe as fallback.
            String notes = item.getNotes();
            if (!ValueUtils.hasValue(notes)
                && Hibernate.unproxy(item.getIngredient()) instanceof Recipe r) {
                notes = r.getDirections();
            }
            if (ValueUtils.hasValue(notes)) {
                print("");
                notes.lines()
                        .forEach(this::print);
            }
        }

    }

}
