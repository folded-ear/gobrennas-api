package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    @Autowired
    private LabelService labelService;

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
                                             Sort sort,
                                             int offset,
                                             int limit) {
        return pantryItemRepository.search(
                PantryItemSearchRequest.builder()
                        .filter(filter)
                        .sort(sort)
                        .offset(offset)
                        .limit(limit)
                        .build());
    }

    public PantryItem renameItem(Long id,
                                 String name) {
        var item = getItem(id);
        item.setName(name);
        return pantryItemRepository.save(item);
    }

    @NotNull
    private PantryItem getItem(Long id) {
        return pantryItemRepository.findById(id)
                .orElseThrow();
    }

    public PantryItem addLabel(Long id,
                               String label) {
        var item = getItem(id);
        labelService.addLabel(item, label);
        return pantryItemRepository.save(item);
    }

    public PantryItem removeLabel(Long id,
                                  String label) {
        var item = getItem(id);
        labelService.removeLabel(item, label);
        return pantryItemRepository.save(item);
    }

    public PantryItem addSynonym(Long id,
                                 String synonym) {
        var item = getItem(id);
        item.addSynonym(synonym);
        return pantryItemRepository.save(item);
    }

    public PantryItem removeSynonym(Long id,
                                    String synonym) {
        var item = getItem(id);
        item.removeSynonym(synonym);
        return pantryItemRepository.save(item);
    }

}
