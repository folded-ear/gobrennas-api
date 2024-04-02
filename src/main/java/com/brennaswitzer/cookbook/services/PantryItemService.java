package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.PantryItem_;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
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
                .filter(it -> it.getStoreOrder() > 0 || it.getId().equals(targetId))
                .filter(it -> !it.getId().equals(active.getId()))
                .sorted(PantryItem.BY_STORE_ORDER)
                .forEachOrdered(it -> {
                    if (it.getId().equals(targetId)) {
                        if (after) {
                            it.setStoreOrder(seq.incrementAndGet());
                            active.setStoreOrder(seq.incrementAndGet());
                        } else {
                            active.setStoreOrder(seq.incrementAndGet());
                            it.setStoreOrder(seq.incrementAndGet());
                        }
                    } else {
                        it.setStoreOrder(seq.incrementAndGet());
                    }
                });
    }

    public SearchResponse<PantryItem> search(String filter,
                                             List<String> sortBy,
                                             int offset,
                                             int limit) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = Collections.singletonList(PantryItem_.NAME);
        }
        return pantryItemRepository.search(
                PantryItemSearchRequest.builder()
                        .filter(filter)
                        .sort(Sort.by(sortBy.toArray(String[]::new)))
                        .offset(offset)
                        .limit(limit)
                        .build());
    }

}
