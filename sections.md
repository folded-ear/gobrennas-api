# Sections

A _section_ is a "partial recipe" which doesn't stand on its own. For example, you might have frosting as a
section of a german chocolate cake recipe in your library. On the other hand, you might consider buttercream icing to be
a recipe itself, only loosely associated with various other recipes.

This distinction is entirely in the mind of the cook, and varies contextually. Continuing the example, buttercream
"becomes" a section of the cupcake recipe you're planning Clara's birthday party this weekend. When you're making the
cupcakes, the icing is a section of the cupcakes recipe, even though they're separate in your library.

## Entity Model

Sections are stored as recipes, with "section-ness" represented implicitly. _Owned_ sections (frosting) are
"part of" their _owning_ recipe (german chocolate cake). Recipes (buttercream icing) may be _by reference_ sections of
other recipes (cupcakes). An _owned_ section may also be the referent of _by reference_ sections (reuse german chocolate
cake's frosting for cupcakes, without 'promoting' frosting to a top-level recipe).

1. Add a new `Recipe sectionOf;` field to the `Recipe` class, backed by a `ON DELETE RESTRICT` FK column in the
   database.
    1. The value indicates the recipe the section is _owned_ by.
    1. Null means the recipe is not an _owned_ section.
1. Add a new `boolean section;` field to the `IngredientRef` class, for whether the ref is a section. Whether _owned_ or
   _by reference_ is not differentiated.
1. _By reference_ sections are normal entity associations (aka today's subrecipes).
1. _Owned_ sections are as if composed into their _owning_ recipe.
    1. When an _owned_ section is removed from a recipe, it is either:
        1. deleted if not otherwise referenced, or
        1. becomes _owned by_ an arbitrary referrer.
    1. When a recipe is deleted, its _owned_ sections are removed first.
1. An _owned_ section can be promoted to a top-level recipe, which converts the section to be _by reference_.
1. A _by reference_ section can be demoted to an _owned_ section, as long as the referent is not already _owned_.

This model yields two different definitions of "section": a recipe _owned by_ another (w/ `sectionOf` set), and a
_reference_ from one recipe to another (an `IngredientRef` referring to a `Recipe`). This mirrors the two different
contextual viewpoints in the cook's mind. It's also the reason for all the italics.

## GraphQL Schema

1. Sections (_owned_ or _by reference_) are recipes; they can always be retrieved via `LibraryQuery.getRecipeById`and
   `bulkIngredients`.
1. _Owned_ sections will not be returned from `LibraryQuery.recipes` or `.suggestRecipesToCook`.
1. Create a `LibrarySearch` input type with `LibraryQuery.recipes`'s params and accept that instead.

1. Add a new `Section` type, sharing `id`, `name`, `direction`, `ingredients`, and `labels` with `Recipe`, plus a
   `sectionOf: ID`.
1. Add a new `sections: [Section!]!` to `Recipe`.
1. Add a new `LibraryQuery.sections` field which also accepts `LibrarySearch` and only returns _owned_ sections
   (as a new `SectionConnection` and friends).

1. Add a new `SectionInfo` input type, sharing `id`, `name`, `direction`, `ingredients`, and `labels` with
   `IngredientInfo`. _Owned_ sections will always have the data fields populated, as well as `id` once persistent. _By
   reference_ sections will only ever have `id`.
1. Add a new `sections: [SectionInfo!]` to `IngredientInfo`.

## Library

### Search

1. Exclude _owned_ sections from search results.
1. Exclude _owned_ sections from recommendations.

### Recipe Display

1. _Owned_ sections participate in scaling, while _by reference_ sections do not.
1. Sections with non-blank description float to the top w/ collapse as subrecipes do today.
1. Sections with no description fall to the bottom of the ingredient list, title as subheading.

### Recipe Form

1. New "Add Section" button, to either:
    1. create a blank _owned_ section, or
   1. search for _owned_ sections to include _by reference_.
       1. This needs a toggle to search for recipes, instead of _owned_ sections. Or between _owned_ sections and
          everything?
1. _Owned_ sections contain title, ingredients, directions, and labels.
    1. Title is required and gets defaulted to "MOAR Goober Whosit" or something.
    1. Ingredients are optional, with a blank ElEdit row visible to start.
        1. Cannot have _owned_ subsections, but can have _by reference_ subsections.
    1. Directions and labels are optional, and should start collapsed if empty (with an icon to toggle visibility).
1. _By reference_ sections:
    1. are read only,
    1. show a link to their top-level recipe, which is the section itself if not _owned_, and
    1. can be one-click-replaced by a new _owned_ section of the current recipe, duplicating the underlying recipe, as
       long as it doesn't have _owned_ sections.
1. Adding a recipe as an ingredient creates a _by reference_ section (as today).
    1. Recog suggestions (for ElEdit) exclude sections (like search results).
1. Non-section ingredients can be dragged between _owned_ sections and/or the top-level recipe.
1. Sections can be reordered, separate from reordering their ingredients. On the form, they don't float/fall.
1. Sections can be removed from the recipe.
1. _Owned_ sections have a "Used By" button, if any other recipe includes them _by reference_, which opens a dialog to
   enumerate referrers. Should all sections?

### Textract

1. **FUTURE:** New "New Section" action button, which will create a new (name-defaulted) section and add the selected
   text as its ingredients.
1. **FUTURE:** make the existing buttons into combo buttons which allow targeting a section, instead of the top-level
   recipe, if any sections exist.

## Planner

_Owned_ sections shouldn't have a "Cook" button.

### Cook Planned Recipe or Bucket

1. Remove ingredient scaling UI; it's too late.
1. Sections with non-blank description float to the top w/ collapse as subrecipes do today.
1. Sections with no description fall to the bottom of the ingredient list, title as subheading.
1. Hide "I Cooked It!" and "Open Library Recipe" for
    1. _owned_ sections if the top-level recipe is also visible, and
    1. all sections without a description.

## Shopping

No special handling.

**FUTURE:** Adjust the attribution breadcrumbs, so section names come after the top-level recipe? Using sugar for
the baking example:

* sugar (10 c)
    * 1 c sugar<br>German Chocolate Cake / Our Week
    * 2 c sugar<br>Buttercream Icing / Cupcakes / Clara's Birthday / Our Week
    * 3 c sugar<br>Cupcakes / Clara's Birthday / Our Week
    * 4 c sugar<br>Frosting / German Chocolate Cake / Our Week

I believe the frosting attribution would be better as "German Chocolate Cake - Frosting / Our Week". This gets awkward
with Clara's cupcakes, as the same rule would yield "Cupcakes - Buttercream Icing / Clara's Birthday / Our Week" for the
icing (reasonable) and "Clara's Birthday - Cupcakes / Our Week" for the cupcakes themselves (less reasonable). This
might need to pass section ownership information from library to planner, so only frosting gets flipped around. Or maybe
everything should always be top-down, period?

Note that the "Our Week" breadcrumb is only visible if you're shopping multiple plans, and "Clara's Birthday" might be a
bucket, not a plan item.

## Plan Sidebar (and Calendar)

1. **ASSUMPTION:** Sections usually stay with their recipe, so if you want to move them, having to open the full planner
   is reasonable for the reduced clutter of the sidebar. Hide planned recipes which:
    1. are a section,
    1. are a descendant of their aggregate, and
    1. are in the same bucket as their aggregate, or not bucketed.

## Pantry Item Admin

1. The "Uses" popup needs to include the top-level recipe's name for _owned_ sections.
    1. Include "and 7 other referrers"?
    1. For _by reference_ sections too?

## Discussion

1. A quantity of an _owned_ section makes no sense; it's part of this recipe.
1. A quantity of a _by reference_ section makes a lot of sense.
    1. If you want to add buttercream icing to a recipe, you'd want to multiply across the buttercream recipe's yield.
       E.g., if buttercream yields 4 c icing, but I only need 2 c for my recipe, I want "1/2 buttercream icing"
1. A quantity of a _by reference_ section targeting an _owned_ section is ... uh
    1. If you want to reuse german chocolate cake's coconut frosting for cupcakes, there's no yield for the frosting
       alone. If the cake recipe makes two 9-inch layers, but your cupcake recipe makes 12, you need a _lot_ less
       frosting... but there's no yield on a section to multiple across.
