package com.supermarket.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("store")
@Schema(description = "店铺实体")
public class Store {

    @Schema(description = "店铺ID")
    @TableId(type = IdType.AUTO)
    @ExcelProperty("店铺ID")
    private Long id;

    @Schema(description = "店铺名称")
    @ExcelProperty("店铺名称")
    private String name;

    @Schema(description = "店铺地址")
    @ExcelProperty("店铺地址")
    private String address;

    @Schema(description = "联系电话")
    @ExcelProperty("联系电话")
    private String phone;

    @Schema(description = "店铺图片URL")
    @ExcelProperty("店铺图片")
    private String image;

    @Schema(description = "店铺状态：0-关闭，1-营业")
    @ExcelProperty("状态")
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
