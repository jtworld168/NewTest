package com.supermarket.vo;

import com.supermarket.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品视图对象")
public class ProductVO extends Product {

    @Schema(description = "分类名称")
    private String categoryName;
}
