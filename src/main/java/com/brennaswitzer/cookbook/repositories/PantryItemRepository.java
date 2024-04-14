package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

public interface PantryItemRepository extends CrudRepository<PantryItem, Long>, PantryItemSearchRepository {

    @Query("""
           select item
           from PantryItem item
              left join item.synonyms syn
           where upper(item.name) = upper(:name)
              or upper(syn) = upper(:name)
           order by item.id
           """)
    List<PantryItem> findByNameIgnoreCaseOrderById(String name);

    @Query("""
           select item
           from PantryItem item
              left join item.synonyms syn
           where upper(item.name) like upper('%' || :name || '%') escape '\\'
              or upper(syn) like upper('%' || :name || '%') escape '\\'
           order by item.id
           """)
    List<PantryItem> findAllByNameIgnoreCaseContainingOrderById(String name);

    List<PantryItem> findAllByUpdatedAtIsAfter(Instant cutoff);

}
