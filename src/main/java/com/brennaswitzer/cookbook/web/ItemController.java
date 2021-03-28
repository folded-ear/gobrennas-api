package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.payload.ItemToRecognize;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;

@RestController
@MessageMapping("/item")
public class ItemController {

    @Autowired
    private ItemService service;

    @MessageMapping("/recognize")
    @SendToUser(broadcast = false)
    public RecognizedItem recognizeItem(@Payload ItemToRecognize item) {
        return service.recognizeItem(item.getRaw(), item.getCursor());
    }

}
