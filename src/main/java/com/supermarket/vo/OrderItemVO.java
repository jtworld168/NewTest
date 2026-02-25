package com.supermarket.vo;

import com.supermarket.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单明细视图对象")
public class OrderItemVO extends OrderItem {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品条码")
    private String productBarcode;
}
