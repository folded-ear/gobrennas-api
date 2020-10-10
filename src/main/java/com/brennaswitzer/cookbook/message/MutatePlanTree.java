package com.brennaswitzer.cookbook.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutatePlanTree {

    private List<Long> ids;
    private Long parentId;
    private Long afterId;

}
