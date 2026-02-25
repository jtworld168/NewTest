package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.OperationLog;
import com.supermarket.mapper.OperationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "操作日志", description = "操作日志接口（MongoDB）")
@RestController
@RequestMapping("/api/logs")
@ConditionalOnBean(MongoTemplate.class)
public class OperationLogController {

    @Autowired
    private OperationLogRepository logRepository;

    @Operation(summary = "查询所有日志")
    @GetMapping("/list")
    public Result<List<OperationLog>> list() {
        return Result.success(logRepository.findAll());
    }

    @Operation(summary = "根据模块查询日志")
    @GetMapping("/getByModule/{module}")
    public Result<List<OperationLog>> getByModule(@PathVariable String module) {
        return Result.success(logRepository.findByModuleOrderByCreateTimeDesc(module));
    }
}
