package com.supermarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "多商品订单创建DTO")
public class MultiItemOrderCreateDTO {
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(description = "店铺ID")
    private Long storeId;
    @Schema(description = "用户优惠券ID（可选）")
    private Long userCouponId;
    @Schema(description = "商品明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemDTO> items;

    @Data
    @Schema(description = "订单商品明细")
    public static class OrderItemDTO {
        @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productId;
        @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer quantity;
    }
}
