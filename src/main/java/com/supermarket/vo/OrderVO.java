package com.supermarket.vo;

import com.supermarket.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单视图对象")
public class OrderVO extends Order {

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品条码")
    private String productBarcode;
}
