package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.domain.InventoryTx;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.IngredientRefInfo;
import com.brennaswitzer.cookbook.payload.InventoryTxInfo;
import com.brennaswitzer.cookbook.repositories.IngredientRepository;
import com.brennaswitzer.cookbook.repositories.InventoryItemRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private EntityManager entityManager;

    public Slice<InventoryItem> listInventory(Pageable page) {
        return itemRepository.findByUser(principalAccess.getUser(), page);
    }

    public InventoryItem ensureInventoryItem(User user, Ingredient ingredient) {
        return itemRepository
                .findByUserAndIngredient(user, ingredient)
                .orElseGet(() ->
                        itemRepository.save(new InventoryItem(user, ingredient)));
    }

    public InventoryTx createTransaction(InventoryTxInfo info) {
        val user = principalAccess.getUser();
        val ing = getIngredientForInfo(info);
        val item = ensureInventoryItem(user, ing);
        return item.transaction(info.getType(), info.extractQuantity(entityManager));
    }

    private Ingredient getIngredientForInfo(IngredientRefInfo info) {
        if (info.hasIngredientId()) {
            val oIng = ingredientRepository.findById(info.getIngredientId());
            if (oIng.isPresent()) return oIng.get();
        }
        return ingredientService.ensureIngredientByName(info.getIngredient());
    }

}
