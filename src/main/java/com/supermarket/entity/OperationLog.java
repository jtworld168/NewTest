package com.supermarket.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "operation_logs")
public class OperationLog {
    @Id
    private String id;
    private String module;
    private String operation;
    private String operator;
    private String detail;
    private LocalDateTime createTime;
}
