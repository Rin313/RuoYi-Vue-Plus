package org.dromara.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssetType {
    BALANCE("balance"),
    COMMISSION("commission_balance"),
    COIN("coin_balance"),
    POINT("point_balance");    
    final String field;
}
