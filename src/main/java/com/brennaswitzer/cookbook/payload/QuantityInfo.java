package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuantityInfo {

    private Double quantity;
    @Deprecated
    private String units;
    private Long uomId;

}
