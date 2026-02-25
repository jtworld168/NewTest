package com.supermarket.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`user`")
@Schema(description = "用户实体")
public class User {

    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    @ExcelProperty("用户ID")
    private Long id;

    @Schema(description = "用户名")
    @ExcelProperty("用户名")
    private String username;

    @Schema(description = "密码")
    @ExcelIgnore
    private String password;

    @Schema(description = "手机号")
    @ExcelProperty("手机号")
    private String phone;

    @Schema(description = "角色：ADMIN-管理员，EMPLOYEE-员工，CUSTOMER-顾客")
    @ExcelProperty("角色")
    private UserRole role;

    @Schema(description = "是否酒店员工")
    @ExcelProperty("是否酒店员工")
    private Boolean isHotelEmployee;

    @Schema(description = "用户头像URL")
    @ExcelIgnore
    private String avatar;

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
