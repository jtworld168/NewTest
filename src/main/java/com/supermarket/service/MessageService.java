package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
    boolean sendMessage(Message message);
    boolean broadcastMessage(String title, String content, String type);
    List<Message> getMessagesByUserId(Long userId);
    int getUnreadCount(Long userId);
    boolean markAsRead(Long id);
    boolean markAllAsRead(Long userId);
    IPage<Message> listPage(int pageNum, int pageSize);
    boolean deleteMessage(Long id);
    boolean deleteBatchMessages(List<Long> ids);
    List<Message> listAll();
}
