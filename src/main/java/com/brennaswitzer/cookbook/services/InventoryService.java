package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.repositories.InventoryItemRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public Slice<InventoryItem> listInventory(Pageable page) {
        return itemRepository.findByUser(principalAccess.getUser(), page);
    }
}
