package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.CompoundQuantity;
import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.InventoryItemInfo;
import com.brennaswitzer.cookbook.payload.Page;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@WithAliceBobEve
public class InventoryControllerTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void doesItSmoke() throws Exception {
        RecipeBox box = new RecipeBox();
        User owner = principalAccess.getUser();
        box.persist(entityManager, owner);
        InventoryItem salt = new InventoryItem(owner, box.salt);
        salt.acquire(new Quantity(3, box.lbs));
        salt.consume(new Quantity(1, box.tbsp));
        salt.consume(new CompoundQuantity(new Quantity(1, box.cup)).minus(new Quantity(1, box.tsp)));
        entityManager.persist(salt);
        val body = mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        System.out.println(body);
        val type = objectMapper.getTypeFactory().constructParametricType(Page.class, InventoryItemInfo.class);
        val obj = objectMapper.readValue(body, type);
        val d = 1;
    }

}
