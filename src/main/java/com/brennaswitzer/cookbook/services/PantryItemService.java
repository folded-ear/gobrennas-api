package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class PantryItemService {

    @Autowired
    private PantryItemRepository pantryItemRepository;

    public PantryItem saveOrUpdatePantryItem(PantryItem item) {
        return pantryItemRepository.save(item);
    }

    public Iterable<PantryItem> findAllPantryItems() {
        return pantryItemRepository.findAll();
    }

    public List<PantryItem> findAllByUpdatedAtIsAfter(Instant cutoff) {
        return pantryItemRepository.findAllByUpdatedAtIsAfter(cutoff);
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
    }

}
