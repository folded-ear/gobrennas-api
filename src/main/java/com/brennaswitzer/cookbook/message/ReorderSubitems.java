package com.brennaswitzer.cookbook.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderSubitems {

    private Long id;
    private List<Long> subitemIds;

}
