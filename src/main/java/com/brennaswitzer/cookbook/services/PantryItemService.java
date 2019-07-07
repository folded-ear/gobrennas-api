package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.services.events.TaskCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class PantryItemService {

    @Autowired
    private PantryItemRepository pantryItemRepository;

    public PantryItem saveOrUpdatePantryItem( PantryItem item) {
        return pantryItemRepository.save(item);
    }

    public Iterable<PantryItem> findAllPantryItems() {
        return pantryItemRepository.findAll();
    }

    @EventListener
    public void taskCompleted(TaskCompletedEvent e) {
        System.out.println("YO! WOO! Task #" + e.getId() + " was completed!");
    }

}
