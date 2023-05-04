package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.PlanItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({
        "SpringJavaAutowiredFieldsWarningInspection",
        "UnusedReturnValue"})
@Service
@Transactional
public class TaskService {

    @Autowired
    private PlanService planService;

    @Autowired
    private ItemService itemService;

    public PlanItem renameItem(Long id, String name) {
        PlanItem it = planService.getPlanItemById(id, AccessLevel.CHANGE);
        it.setName(name);
        itemService.updateAutoRecognition(it);
        return it;
    }

}
