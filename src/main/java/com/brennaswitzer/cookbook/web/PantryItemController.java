package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.message.OrderForStore;
import com.brennaswitzer.cookbook.services.PantryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/pantryitem")
@MessageMapping("/pantry-item")
public class PantryItemController {

    @Autowired
    private PantryItemService pantryItemService;

    @GetMapping("/all")
    public Iterable<PantryItem> getAllPantryItems() {
        return pantryItemService.findAllPantryItems();
    }

    @PostMapping("")
    public ResponseEntity<?> createNewPantryItem(@Valid @RequestBody PantryItem item, BindingResult result) {
        PantryItem newItem = pantryItemService.saveOrUpdatePantryItem(item);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    @MessageMapping("/order-for-store")
    public void orderForStore(@Payload OrderForStore action) {
        Long id = action.getId();
        Long targetId = action.getTargetId();
        if (id == null || targetId == null || id.equals(targetId)) {
            throw new IllegalArgumentException("Can only order to for two different non-null ingredient IDs");
        }
        pantryItemService.orderForStore(id, targetId, action.isAfter());
    }

}
