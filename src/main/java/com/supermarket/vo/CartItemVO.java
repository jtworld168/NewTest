package com.supermarket.vo;

import com.supermarket.entity.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "购物车视图对象")
public class CartItemVO extends CartItem {

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品条码")
    private String productBarcode;
}
