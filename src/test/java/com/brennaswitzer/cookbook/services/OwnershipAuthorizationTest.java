package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Rating;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.support.Info2Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.textract.TextractService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WithAliceBobEve
class OwnershipAuthorizationTest {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private Info2Recipe info2Recipe;

    @Autowired
    private RecipeHistoryService historyService;

    @Autowired
    private TextractService textractService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private EntityManager entityManager;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = userRepo.getByName("Alice");
        bob = userRepo.getByName("Bob");
    }

    @Test
    void othersRecipe() {
        Recipe pizza = recipe(bob);
        IngredientInfo info = new IngredientInfo();
        info.setId(pizza.getId());

        assertThrows(AccessDeniedException.class,
                     () -> recipeService.setRecipePhoto(pizza.getId(), null, null));
        assertThrows(AccessDeniedException.class,
                     () -> recipeService.updateRecipe(pizza, info));
        assertThrows(AccessDeniedException.class,
                     () -> recipeService.deleteRecipeById(pizza.getId()));
        assertThrows(AccessDeniedException.class,
                     () -> info2Recipe.convert(principalAccess.getUserPrincipal(), info));
    }

    @Test
    void ownRecipe() {
        Recipe pizza = recipe(alice);

        assertSame(pizza,
                   recipeService.setRecipePhoto(pizza.getId(), null, null));
    }

    @Test
    void missingRecipe() {
        assertThrows(EntityNotFoundException.class,
                     () -> recipeService.setRecipePhoto(-1L, null, null));
    }

    @Test
    void othersHistory() {
        Recipe pizza = recipe(bob);
        PlannedRecipeHistory h = history(pizza, bob);

        assertThrows(AccessDeniedException.class,
                     () -> historyService.setNotes(pizza.getId(), h.getId(), "yum"));
        assertThrows(AccessDeniedException.class,
                     () -> historyService.setRating(pizza.getId(), h.getId(), Rating.FIVE_STARS));
        assertNull(h.getNotes());
        assertNull(h.getRating());
    }

    @Test
    void ownHistory() {
        Recipe pizza = recipe(bob);
        PlannedRecipeHistory h = history(pizza, alice);

        historyService.setNotes(pizza.getId(), h.getId(), "yum");

        assertEquals("yum", h.getNotes());
    }

    @Test
    void othersJob() {
        TextractJob job = job(bob);

        assertThrows(AccessDeniedException.class,
                     () -> textractService.getJob(job.getId()));
        assertThrows(AccessDeniedException.class,
                     () -> textractService.deleteJob(job.getId()));
    }

    @Test
    void ownJob() {
        TextractJob job = job(alice);

        assertSame(job, textractService.getJob(job.getId()));
    }

    private Recipe recipe(User owner) {
        Recipe r = new Recipe("Pizza");
        r.setOwner(owner);
        entityManager.persist(r);
        return r;
    }

    private PlannedRecipeHistory history(Recipe recipe, User owner) {
        var h = new PlannedRecipeHistory();
        h.setRecipe(recipe);
        h.setOwner(owner);
        h.setPlanItemId(1L);
        h.setPlannedAt(Instant.now());
        h.setDoneAt(Instant.now());
        h.setStatus(PlanItemStatus.COMPLETED);
        entityManager.persist(h);
        return h;
    }

    private TextractJob job(User owner) {
        var job = new TextractJob();
        job.setOwner(owner);
        job.setPhoto(new S3File("photo", "image/jpeg", 1L));
        entityManager.persist(job);
        return job;
    }

}
