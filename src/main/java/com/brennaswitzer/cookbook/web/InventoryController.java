package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.mapper.InventoryItemMapper;
import com.brennaswitzer.cookbook.mapper.SliceMapper;
import com.brennaswitzer.cookbook.payload.InventoryItemInfo;
import com.brennaswitzer.cookbook.payload.Page;
import com.brennaswitzer.cookbook.services.InventoryService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService service;

    @Autowired
    private InventoryItemMapper itemMapper;

    @Autowired
    private SliceMapper sliceMapper;

    @RequestMapping(method = RequestMethod.GET)
    Page<InventoryItemInfo> listInventory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "25") int pageSize,
            @RequestParam(name = "sort", defaultValue = "pantryItem.name") String sort,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        val inv = service.listInventory(PageRequest.of(
                page,
                pageSize,
                Sort.by(
                        "desc".equalsIgnoreCase(sortDir)
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC,
                        sort)
        ));
        return sliceMapper.sliceToPage(inv, itemMapper::itemToInfo);
    }

}
