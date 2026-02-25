package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.NotificationService;
import com.supermarket.websocket.NotificationWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "消息推送", description = "WebSocket消息推送管理接口")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationWebSocketHandler webSocketHandler;

    @Operation(summary = "发送系统通知给指定用户")
    @PostMapping("/send")
    public Result<Void> sendNotification(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "通知标题") @RequestParam String title,
            @Parameter(description = "通知内容") @RequestParam String content) {
        notificationService.notifyUser(userId, title, content);
        return Result.success();
    }

    @Operation(summary = "广播消息给所有在线用户")
    @PostMapping("/broadcast")
    public Result<Void> broadcast(
            @Parameter(description = "通知标题") @RequestParam String title,
            @Parameter(description = "通知内容") @RequestParam String content) {
        notificationService.broadcastMessage(title, content);
        return Result.success();
    }

    @Operation(summary = "获取当前在线用户数")
    @GetMapping("/online-count")
    public Result<Map<String, Integer>> getOnlineCount() {
        return Result.success(Map.of("count", webSocketHandler.getOnlineCount()));
    }
}
