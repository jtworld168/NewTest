package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
@Schema(description = "消息实体")
public class Message {

    @Schema(description = "消息ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "接收用户ID（null表示广播）")
    private Long userId;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型：SYSTEM-系统消息，ORDER-订单消息，COUPON-优惠券消息")
    private String type;

    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "是否删除")
    @TableLogic
    private Integer deleted;
}
