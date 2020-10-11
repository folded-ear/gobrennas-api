package com.brennaswitzer.cookbook.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanTreeItem {

    private Object id;
    private Long parentId;
    private Long afterId;
    private String name;

}
