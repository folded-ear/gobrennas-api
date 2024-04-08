package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.BaseEntity;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class PantryItemService {

    @Autowired
    private PantryItemRepository pantryItemRepository;
    @Autowired
    private LabelService labelService;
    @Autowired
    private PantryItemCombiner combiner;

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

    @PreAuthorize("hasRole('DEVELOPER')")
    @Transactional(readOnly = true) // GraphQL manages txns imperatively for OSIV
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

    @PreAuthorize("hasRole('DEVELOPER')")
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

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem addLabel(Long id,
                               String label) {
        var item = getItem(id);
        labelService.addLabel(item, label);
        return pantryItemRepository.save(item);
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem removeLabel(Long id,
                                  String label) {
        var item = getItem(id);
        labelService.removeLabel(item, label);
        return pantryItemRepository.save(item);
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem addSynonym(Long id,
                                 String synonym) {
        var item = getItem(id);
        item.addSynonym(synonym);
        return pantryItemRepository.save(item);
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem removeSynonym(Long id,
                                    String synonym) {
        var item = getItem(id);
        item.removeSynonym(synonym);
        return pantryItemRepository.save(item);
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem combineItems(List<Long> ids) {
        if (ids == null || ids.size() < 2) {
            throw new IllegalArgumentException("Cannot combine fewer than two items");
        }
        // this is inefficient when more than two, but "don't care."
        return ids.stream()
                .map(pantryItemRepository::findById)
                .map(Optional::orElseThrow)
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .reduce(combiner::combineItems)
                .orElseThrow();
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public boolean deleteItem(Long id) {
        pantryItemRepository.deleteById(id);
        return true;
    }

}
