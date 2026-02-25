package com.supermarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "商品创建DTO")
public class ProductCreateDTO {
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;
    @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;
    @Schema(description = "商品描述")
    private String description;
    @Schema(description = "分类ID")
    private Long categoryId;
    @Schema(description = "员工折扣率")
    private BigDecimal employeeDiscountRate;
    @Schema(description = "商品条形码")
    private String barcode;
    @Schema(description = "商品图片URL")
    private String image;
    @Schema(description = "上架状态：0-下架，1-上架")
    private Integer status;
    @Schema(description = "库存预警阈值")
    private Integer stockAlertThreshold;
}
