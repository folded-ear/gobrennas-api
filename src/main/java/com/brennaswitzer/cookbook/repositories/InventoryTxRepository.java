package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.domain.InventoryTx;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

public interface InventoryTxRepository extends BaseEntityRepository<InventoryTx> {

    Iterable<InventoryTx> findByItem(InventoryItem item);

    Iterable<InventoryTx> findByItem(InventoryItem item, Sort sort);

    Slice<InventoryTx> findByItem(InventoryItem item, Pageable pageable);

}
