package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Message;
import com.supermarket.service.MessageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "消息管理", description = "消息发送与查询接口")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "发送消息给指定用户")
    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody Message message) {
        if (message.getUserId() == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (message.getTitle() == null || message.getTitle().isBlank()) {
            return Result.badRequest("消息标题不能为空");
        }
        if (message.getContent() == null || message.getContent().isBlank()) {
            return Result.badRequest("消息内容不能为空");
        }
        return messageService.sendMessage(message) ? Result.success() : Result.error("发送失败");
    }

    @Operation(summary = "一键广播消息给所有用户")
    @PostMapping("/broadcast")
    public Result<Void> broadcast(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String content = body.get("content");
        String type = body.get("type");
        if (title == null || title.isBlank()) {
            return Result.badRequest("消息标题不能为空");
        }
        if (content == null || content.isBlank()) {
            return Result.badRequest("消息内容不能为空");
        }
        return messageService.broadcastMessage(title, content, type) ? Result.success() : Result.error("广播失败");
    }

    @Operation(summary = "获取用户消息列表")
    @GetMapping("/user/{userId}")
    public Result<List<Message>> getUserMessages(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(messageService.getMessagesByUserId(userId));
    }

    @Operation(summary = "获取用户未读消息数")
    @GetMapping("/unread/{userId}")
    public Result<Map<String, Integer>> getUnreadCount(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(Map.of("count", messageService.getUnreadCount(userId)));
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@Parameter(description = "消息ID") @PathVariable Long id) {
        return messageService.markAsRead(id) ? Result.success() : Result.error("操作失败");
    }

    @Operation(summary = "标记用户所有消息为已读")
    @PutMapping("/readAll/{userId}")
    public Result<Void> markAllAsRead(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return messageService.markAllAsRead(userId) ? Result.success() : Result.error("操作失败");
    }

    @Operation(summary = "分页查询消息列表")
    @GetMapping("/listPage")
    public Result<IPage<Message>> listPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "查询所有消息")
    @GetMapping("/list")
    public Result<List<Message>> listAll() {
        return Result.success(messageService.listAll());
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteMessage(@Parameter(description = "消息ID") @PathVariable Long id) {
        return messageService.deleteMessage(id) ? Result.success() : Result.error("删除失败");
    }

    @Operation(summary = "批量删除消息")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchMessages(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return messageService.deleteBatchMessages(ids) ? Result.success() : Result.error("批量删除失败");
    }
}
