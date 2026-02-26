package com.supermarket.vo;

import com.supermarket.entity.StoreProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "店铺商品视图对象")
public class StoreProductVO extends StoreProduct {

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "商品名称")
    private String productName;

}
