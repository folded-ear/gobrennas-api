package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.TxType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryTxInfo extends IngredientRefInfo {
    private TxType type;
}
