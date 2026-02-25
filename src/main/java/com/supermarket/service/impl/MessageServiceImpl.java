package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Message;
import com.supermarket.entity.User;
import com.supermarket.mapper.MessageMapper;
import com.supermarket.service.MessageService;
import com.supermarket.service.UserService;
import com.supermarket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    public boolean sendMessage(Message message) {
        if (message.getIsRead() == null) {
            message.setIsRead(0);
        }
        if (message.getType() == null) {
            message.setType("SYSTEM");
        }
        boolean saved = save(message);
        if (saved && message.getUserId() != null) {
            try {
                notificationService.notifyUser(message.getUserId(), message.getTitle(), message.getContent());
            } catch (Exception ignored) {}
        }
        return saved;
    }

    @Override
    public boolean broadcastMessage(String title, String content, String type) {
        List<User> users = userService.listAll();
        for (User user : users) {
            Message msg = new Message();
            msg.setUserId(user.getId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setType(type != null ? type : "SYSTEM");
            msg.setIsRead(0);
            save(msg);
        }
        try {
            notificationService.broadcastMessage(title, content);
        } catch (Exception ignored) {}
        return true;
    }

    @Override
    public List<Message> getMessagesByUserId(Long userId) {
        return list(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime));
    }

    @Override
    public int getUnreadCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    @Override
    public boolean markAsRead(Long id) {
        return update(new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, id)
                .set(Message::getIsRead, 1));
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        return update(new LambdaUpdateWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));
    }

    @Override
    public IPage<Message> listPage(int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Message>().orderByDesc(Message::getCreateTime));
    }

    @Override
    public boolean deleteMessage(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchMessages(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<Message> listAll() {
        return list(new LambdaQueryWrapper<Message>().orderByDesc(Message::getCreateTime));
    }
}
