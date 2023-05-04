package com.brennaswitzer.cookbook.message;

import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import lombok.Data;

@Data
public class SetPlanTreeItemStatus {

    private Long id;
    private PlanItemStatus status;

}
