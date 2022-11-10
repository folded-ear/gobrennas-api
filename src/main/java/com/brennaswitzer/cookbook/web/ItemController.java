package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.payload.ItemToRecognize;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/item")
public class ItemController {

    @Autowired
    private ItemService service;

    @PostMapping("/recognize")
    public RecognizedItem recognizeItem(@RequestBody ItemToRecognize item) {
        return service.recognizeItem(item.getRaw(), item.getCursor());
    }

}
