package com.supermarket.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("store_product")
@Schema(description = "店铺商品实体")
public class StoreProduct {

    @Schema(description = "店铺商品ID")
    @TableId(type = IdType.AUTO)
    @ExcelProperty("ID")
    private Long id;

    @Schema(description = "店铺ID")
    @ExcelProperty("店铺ID")
    private Long storeId;

    @Schema(description = "商品ID（关联总商品表）")
    @ExcelProperty("商品ID")
    private Long productId;

    @Schema(description = "店铺售价（可与总商品表价格不同）")
    @ExcelProperty("店铺售价")
    private BigDecimal storePrice;

    @Schema(description = "店铺库存")
    @ExcelProperty("店铺库存")
    private Integer storeStock;

    @Schema(description = "安全库存阈值")
    @ExcelProperty("安全库存")
    private Integer safetyStock;

    @Schema(description = "关联优惠券模板ID")
    @ExcelProperty("优惠券ID")
    private Long couponId;

    @Schema(description = "上架状态：0-下架，1-上架")
    @ExcelProperty("上架状态")
    private Integer status;

    @Schema(description = "创建时间")
    @ExcelIgnore
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @ExcelIgnore
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    @TableLogic
    @ExcelIgnore
    private Integer deleted;
}
