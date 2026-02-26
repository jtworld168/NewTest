package com.supermarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单创建DTO")
public class OrderCreateDTO {
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;
    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
    @Schema(description = "用户优惠券ID（可选）")
    private Long userCouponId;
    @Schema(description = "店铺ID（可选）")
    private Long storeId;
}
