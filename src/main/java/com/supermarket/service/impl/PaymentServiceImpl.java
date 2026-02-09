package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Payment;
import com.supermarket.enums.PaymentStatus;
import com.supermarket.mapper.PaymentMapper;
import com.supermarket.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Override
    public Payment getPaymentById(Long id) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getId, id);
        return getOne(wrapper);
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId);
        return getOne(wrapper);
    }

    @Override
    public List<Payment> getPaymentsByStatus(PaymentStatus paymentStatus) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentStatus, paymentStatus)
               .orderByDesc(Payment::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Payment getPaymentByTransactionNo(String transactionNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getTransactionNo, transactionNo);
        return getOne(wrapper);
    }

    @Override
    public List<Payment> searchPayments(String keyword) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Payment::getTransactionNo, keyword)
               .orderByDesc(Payment::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<Payment> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Payment::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addPayment(Payment payment) {
        return save(payment);
    }

    @Override
    public boolean updatePayment(Payment payment) {
        return updateById(payment);
    }

    @Override
    public boolean deletePayment(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchPayments(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<Payment> listAll() {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Payment::getCreateTime);
        return list(wrapper);
    }
}
