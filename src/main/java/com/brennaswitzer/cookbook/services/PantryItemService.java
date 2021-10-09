package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.mapper.PantryItemMapper;
import com.brennaswitzer.cookbook.message.IngredientMessage;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class PantryItemService {

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PantryItemMapper itemMapper;

    public PantryItem saveOrUpdatePantryItem(PantryItem item) {
        return pantryItemRepository.save(item);
    }

    public Iterable<PantryItem> findAllPantryItems() {
        return pantryItemRepository.findAll();
    }

    public void orderForStore(Long id, Long targetId, boolean after) {
        AtomicInteger seq = new AtomicInteger(0);
        //noinspection OptionalGetWithoutIsPresent
        PantryItem active = pantryItemRepository.findById(id).get();
        StreamSupport.stream(pantryItemRepository.findAll().spliterator(), false)
                .sorted(PantryItem.BY_STORE_ORDER)
                .forEachOrdered(it -> {
                    if (it.getId().equals(active.getId())) return;
                    if (it.getId().equals(targetId)) {
                        if (after) {
                            ensureStoreOrder(it, seq.incrementAndGet());
                            ensureStoreOrder(active, seq.incrementAndGet());
                        } else {
                            ensureStoreOrder(active, seq.incrementAndGet());
                            ensureStoreOrder(it, seq.incrementAndGet());
                        }
                    } else {
                        ensureStoreOrder(it, seq.incrementAndGet());
                    }
                });
    }

    private void ensureStoreOrder(PantryItem it, int order) {
        if (it.getStoreOrder() == order) return;
        it.setStoreOrder(order);
        IngredientMessage m = new IngredientMessage();
        m.setType("update");
        m.setId(it.getId());
        m.setInfo(itemMapper.pantryItemToInfo(it));
        messagingTemplate.convertAndSend("/topic/pantry-items", m);
    }

}
