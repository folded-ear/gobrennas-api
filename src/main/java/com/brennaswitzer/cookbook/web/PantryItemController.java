package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.mapper.IngredientMapper;
import com.brennaswitzer.cookbook.message.OrderForStore;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.services.PantryItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/pantryitem")
public class PantryItemController {

    @Autowired
    private IngredientMapper ingredientMapper;

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

    @GetMapping("/all-since")
    public List<IngredientInfo> getUpdatedSince(@RequestParam Long cutoff) {
        return pantryItemService.findAllByUpdatedAtIsAfter(Instant.ofEpochMilli(cutoff))
                .stream()
                .map(ingredientMapper::pantryItemToInfo)
                .collect(Collectors.toList());
    }

    @PostMapping("/order-for-store")
    public void orderForStore(@RequestBody OrderForStore action) {
        pantryItemService.orderForStore(action.getId(),
                                        action.getTargetId(),
                                        action.isAfter());
    }

}
