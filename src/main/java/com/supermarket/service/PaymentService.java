package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Payment;
import com.supermarket.enums.PaymentStatus;

import java.util.List;

public interface PaymentService extends IService<Payment> {

    Payment getPaymentById(Long id);

    Payment getPaymentByOrderId(Long orderId);

    List<Payment> getPaymentsByStatus(PaymentStatus paymentStatus);

    Payment getPaymentByTransactionNo(String transactionNo);

    List<Payment> searchPayments(String keyword);

    IPage<Payment> listPage(int pageNum, int pageSize);

    boolean addPayment(Payment payment);

    boolean updatePayment(Payment payment);

    boolean deletePayment(Long id);

    boolean deleteBatchPayments(List<Long> ids);

    List<Payment> listAll();
}
