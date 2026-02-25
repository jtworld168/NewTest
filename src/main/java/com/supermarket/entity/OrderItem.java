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
@TableName("order_items")
@Schema(description = "订单商品明细实体")
public class OrderItem {

    @Schema(description = "订单商品明细ID")
    @TableId(type = IdType.AUTO)
    @ExcelProperty("明细ID")
    private Long id;

    @Schema(description = "订单ID")
    @ExcelProperty("订单ID")
    private Long orderId;

    @Schema(description = "商品ID")
    @ExcelProperty("商品ID")
    private Long productId;

    @Schema(description = "购买数量")
    @ExcelProperty("购买数量")
    private Integer quantity;

    @Schema(description = "下单时单价（已计算员工折扣）")
    @ExcelProperty("购买单价")
    private BigDecimal priceAtPurchase;

    @Schema(description = "小计金额（单价×数量）")
    @ExcelProperty("小计金额")
    private BigDecimal subtotal;

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
