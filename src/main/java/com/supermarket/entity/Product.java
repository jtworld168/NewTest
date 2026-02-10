package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
@Schema(description = "商品实体")
public class Product {

    @Schema(description = "商品ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品价格")
    private BigDecimal price;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "员工折扣率，如0.8表示八折")
    private BigDecimal employeeDiscountRate;

    @Schema(description = "商品条形码")
    private String barcode;

    @Schema(description = "商品图片URL")
    private String image;

    @Schema(description = "上架状态：0-下架，1-上架")
    private Integer status;

    @Schema(description = "库存预警阈值")
    private Integer stockAlertThreshold;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    @TableLogic
    private Integer deleted;
}
