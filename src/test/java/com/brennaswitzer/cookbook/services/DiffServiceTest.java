package com.brennaswitzer.cookbook.services;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiffServiceTest {

    @Test
    void christmasWontonBroth() {
        var left = Stream.of(
                        "Chicken Broth From Scratch",
                        "",
                        "2 chicken,  3 to 3 1/2 pounds, with skin, cut up",
                        "6 celery, stalks , with leaves, cut into chunks",
                        "4 large carrot,  cut into chunks",
                        "4 onion, yellow , peeled and halved",
                        "2 parsley, parsnip or root (optional)",
                        "About 1 dozen large sprigs parsley",
                        "About 1 dozen black peppercorns",
                        null,
                        "4 teaspoon salt, kosher , more to taste",
                        "2 head garlic")
                .toList();
        var right = List.of(
                "Chicken Broth From Scratch",
                "",
                "3 to 3 1/2 pounds chicken legs",
                "3  stalks celery, with leaves, cut into chunks",
                "2  large carrots, cut into chunks",
                "2  yellow onions, peeled and halved",
                "1  parsnip or parsley root (optional)",
                "About 1 dozen large sprigs parsley",
                "About 1 dozen black peppercorns",
                "2  bay leaves",
                "2  teaspoons kosher salt, more to taste",
                "1 _head_ garlic");
        // ensure it matched the log line
        assertEquals(
                "[Chicken Broth From Scratch, , 2 chicken,  3 to 3 1/2 pounds, with skin, cut up, 6 celery, stalks , with leaves, cut into chunks, 4 large carrot,  cut into chunks, 4 onion, yellow , peeled and halved, 2 parsley, parsnip or root (optional), About 1 dozen large sprigs parsley, About 1 dozen black peppercorns, null, 4 teaspoon salt, kosher , more to taste, 2 head garlic] and [Chicken Broth From Scratch, , 3 to 3 1/2 pounds chicken legs, 3  stalks celery, with leaves, cut into chunks, 2  large carrots, cut into chunks, 2  yellow onions, peeled and halved, 1  parsnip or parsley root (optional), About 1 dozen large sprigs parsley, About 1 dozen black peppercorns, 2  bay leaves, 2  teaspoons kosher salt, more to taste, 1 _head_ garlic]",
                String.format("%s and %s", left, right));

        var diff = new DiffService()
                .diffLinesToPatch(left, right);

        assertEquals("""
                     @@ -2,10 +2,11 @@
                     \s
                     -2 chicken,  3 to 3 1/2 pounds, with skin, cut up
                     -6 celery, stalks , with leaves, cut into chunks
                     -4 large carrot,  cut into chunks
                     -4 onion, yellow , peeled and halved
                     -2 parsley, parsnip or root (optional)
                     +3 to 3 1/2 pounds chicken legs
                     +3  stalks celery, with leaves, cut into chunks
                     +2  large carrots, cut into chunks
                     +2  yellow onions, peeled and halved
                     +1  parsnip or parsley root (optional)
                      About 1 dozen large sprigs parsley
                      About 1 dozen black peppercorns
                     -4 teaspoon salt, kosher , more to taste
                     -2 head garlic
                     +2  bay leaves
                     +2  teaspoons kosher salt, more to taste
                     +1 _head_ garlic
                     """,
                     diff);
    }

}
