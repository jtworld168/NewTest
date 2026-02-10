package com.supermarket.mapper;

import com.supermarket.entity.OperationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OperationLogRepository extends MongoRepository<OperationLog, String> {
    List<OperationLog> findByModuleOrderByCreateTimeDesc(String module);
}
