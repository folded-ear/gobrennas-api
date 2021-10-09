package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.CompoundQuantity;
import com.brennaswitzer.cookbook.domain.Quantity;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.Collection;
import java.util.stream.Collectors;

public class QuantityInfo {

    @Getter
    @Setter
    private Double quantity;
    @Getter
    @Setter
    @Deprecated
    private String units;
    @Getter
    @Setter
    private Long uomId;

    public static QuantityInfo from(Quantity q) {
        val info = new QuantityInfo();
        info.setQuantity(q.getQuantity());
        if (q.hasUnits()) {
            info.setUomId(q.getUnits().getId());
            info.setUnits(q.getUnits().getName());
        }
        return info;
    }

    public static Collection<QuantityInfo> from(CompoundQuantity q) {
        return q.getComponents()
                .stream()
                .map(QuantityInfo::from)
                .collect(Collectors.toList());
    }

}
