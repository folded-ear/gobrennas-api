package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.repositories.InventoryItemRepository;
import com.brennaswitzer.cookbook.repositories.InventoryTxRepository;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.ResultSetPrinter;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class InventoryDatabaseTest {

    @Autowired
    private InventoryItemRepository itemRepo;

    @Autowired
    private InventoryTxRepository txRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private JdbcTemplate jdbcTmpl;

    @Test
    public void doesItSmoke() {
        val box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        val salt = itemRepo.save(
                new InventoryItem(
                        principalAccess.getUser(),
                        box.salt
                )
        );

        // I _seriously_ bought salt
        salt.acquire(new Quantity(123456, box.cup));

        // bought some more in here too, but didn't write it down
        for (int i = 4; i <= 19; i++) {
            salt.consume(new Quantity(i, box.tbsp));
        }

        // I just checked; I have 3 cups of salt.
        salt.reset(new Quantity(3, box.cup));

        // used 4 Tbsp (half cup)
        salt.consume(new Quantity(4, box.tbsp));

        val expected = Collections.singleton(new Quantity(2.75, box.cup));
        checkSalt(salt, expected);

        entityManager.flush();
        entityManager.clear();
        assertEquals(1, itemRepo.count());
        assertEquals(19, txRepo.count());

        checkSalt(itemRepo.findById(salt.getId()).get(), expected);

        jdbcTmpl.query(
                "select * from inventory_tx order by created_at, id",
                ResultSetPrinter::printResultSet
        );
    }

    private void checkSalt(InventoryItem salt, Set<Quantity> expected) {
        assertEquals(19, salt.getTxCount());
        assertEquals(19, salt.getTransactions().size());
        Set<Quantity> total = new HashSet<>();
        for (val tx : txRepo.findByItem(
                salt,
                Sort.by(
                        Sort.Order.asc(InventoryTx_.CREATED_AT),
                        Sort.Order.asc(InventoryTx_.ID)
                ))) {
            System.out.println(tx);
            total = tx.computeNewQuantity(total);
        }
        assertEquals(total, salt.getQuantity());
        assertEquals(expected, salt.getQuantity());
    }

}
