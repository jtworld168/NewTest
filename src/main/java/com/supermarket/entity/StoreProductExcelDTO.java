package com.supermarket.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreProductExcelDTO {

    @ExcelProperty("店铺ID")
    private Long storeId;

    @ExcelProperty("商品条码")
    private String barcode;

    @ExcelProperty("商品名称")
    private String productName;

    @ExcelProperty("店铺售价")
    private BigDecimal storePrice;

    @ExcelProperty("店铺库存")
    private Integer storeStock;

    @ExcelProperty("安全库存")
    private Integer safetyStock;

    @ExcelProperty("上架状态")
    private Integer status;
}
