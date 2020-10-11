package com.brennaswitzer.cookbook.message;

import com.brennaswitzer.cookbook.domain.TaskStatus;
import lombok.Data;

@Data
public class SetPlanTreeItemStatus {

    private Long id;
    private TaskStatus status;

}
