package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.BaseEntity;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.services.indexing.PantryItemNeedsDuplicatesFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class PantryItemService {

    @Autowired
    private PantryItemRepository pantryItemRepository;
    @Autowired
    private LabelService labelService;
    @Autowired
    private PantryItemCombiner combiner;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public PantryItem saveOrUpdatePantryItem(PantryItem item) {
        return pantryItemRepository.save(item);
    }

    public Iterable<PantryItem> findAllPantryItems() {
        return pantryItemRepository.findAll();
    }

    public List<PantryItem> findAllByUpdatedAtIsAfter(Instant cutoff) {
        return pantryItemRepository.findAllByUpdatedAtIsAfter(cutoff);
    }

    /**
     * Order a PantryItem relative to another, and return it after the update.
     */
    public PantryItem orderForStore(Long id, Long targetId, boolean after) {
        if (id == null || targetId == null || id.equals(targetId)) {
            throw new IllegalArgumentException("Can only 'order to' for two distinct non-null ingredient IDs");
        }
        AtomicInteger seq = new AtomicInteger(0);
        //noinspection OptionalGetWithoutIsPresent
        PantryItem active = pantryItemRepository.findById(id).get();
        // todo: it's a little silly to pull the entire database into memory...
        pantryItemRepository.findAll()
                .stream()
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
        return active;
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public SearchResponse<PantryItem> search(PantryItemSearchRequest request) {
        return pantryItemRepository.search(request);
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem renameItem(Long id,
                                 String name) {
        var item = getItem(id);
        item.setName(name);
        item = pantryItemRepository.save(item);
        needsDupesFound(item);
        return item;
    }

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
    public PantryItem setLabels(Long id, Set<String> labels) {
        var item = getItem(id);
        labelService.updateLabels(item, labels);
        item = pantryItemRepository.save(item);
        needsDupesFound(item);
        return item;
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
    public PantryItem setSynonyms(Long id, Set<String> synonyms) {
        var item = getItem(id);
        item.clearSynonyms();
        synonyms.forEach(item::addSynonym);
        item = pantryItemRepository.save(item);
        needsDupesFound(item);
        return item;
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem combineItems(List<Long> ids) {
        if (ids == null || ids.size() < 2) {
            throw new IllegalArgumentException("Cannot combine fewer than two items");
        }
        // this is inefficient when more than two, but "don't care."
        PantryItem result = ids.stream()
                .map(pantryItemRepository::findById)
                .map(Optional::orElseThrow)
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .reduce(combiner::combineItems)
                .orElseThrow();
        needsDupesFound(result);
        return result;
    }

    @PreAuthorize("hasRole('DEVELOPER')")
    public PantryItem deleteItem(Long id) {
        var it = pantryItemRepository.getReferenceById(id);
        pantryItemRepository.deleteById(id);
        return it;
    }

    private void needsDupesFound(PantryItem item) {
        eventPublisher.publishEvent(new PantryItemNeedsDuplicatesFound(item));
    }

}
