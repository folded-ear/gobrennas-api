package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.PantryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/pantryitem")
public class PantryItemController {

    @Autowired
    private PantryItemService pantryItemService;

    @PostMapping("")
    public ResponseEntity<?> createNewPantryItem(@Valid @RequestBody PantryItem item, BindingResult result) {
        PantryItem newItem = pantryItemService.saveOrUpdatePantryItem(item);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

}
