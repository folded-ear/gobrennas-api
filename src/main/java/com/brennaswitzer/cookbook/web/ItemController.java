package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.payload.ItemToRecognize;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/item")
//@MessageMapping("/item") // todo: cull
public class ItemController {

    @Autowired
    private ItemService service;

    @PostMapping("/recognize")
    //@MessageMapping("/recognize") // todo: cull
    @SendToUser(broadcast = false)
    public RecognizedItem recognizeItem(@RequestBody @Payload ItemToRecognize item) {
        return service.recognizeItem(item.getRaw(), item.getCursor());
    }

}
