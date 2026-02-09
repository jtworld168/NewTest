package com.supermarket;

import com.supermarket.entity.Payment;
import com.supermarket.enums.PaymentMethod;
import com.supermarket.enums.PaymentStatus;
import com.supermarket.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void testAddAndGetPayment() {
        Payment payment = new Payment();
        payment.setOrderId(1L);
        payment.setAmount(new BigDecimal("99.90"));
        payment.setPaymentMethod(PaymentMethod.WECHAT);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionNo("TXN20260209001");

        assertTrue(paymentService.addPayment(payment));
        assertNotNull(payment.getId());

        Payment found = paymentService.getPaymentById(payment.getId());
        assertNotNull(found);
        assertEquals(new BigDecimal("99.90"), found.getAmount());
        assertEquals(PaymentMethod.WECHAT, found.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, found.getPaymentStatus());
    }

    @Test
    void testGetPaymentByOrderId() {
        Payment payment = new Payment();
        payment.setOrderId(2L);
        payment.setAmount(new BigDecimal("50.00"));
        payment.setPaymentMethod(PaymentMethod.ALIPAY);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTransactionNo("TXN20260209002");
        paymentService.addPayment(payment);

        Payment found = paymentService.getPaymentByOrderId(2L);
        assertNotNull(found);
        assertEquals(PaymentMethod.ALIPAY, found.getPaymentMethod());
    }

    @Test
    void testGetPaymentsByStatus() {
        Payment payment = new Payment();
        payment.setOrderId(3L);
        payment.setAmount(new BigDecimal("30.00"));
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setTransactionNo("TXN20260209003");
        paymentService.addPayment(payment);

        List<Payment> failed = paymentService.getPaymentsByStatus(PaymentStatus.FAILED);
        assertFalse(failed.isEmpty());
    }

    @Test
    void testGetPaymentByTransactionNo() {
        Payment payment = new Payment();
        payment.setOrderId(4L);
        payment.setAmount(new BigDecimal("120.00"));
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionNo("TXN20260209004");
        paymentService.addPayment(payment);

        Payment found = paymentService.getPaymentByTransactionNo("TXN20260209004");
        assertNotNull(found);
        assertEquals(PaymentMethod.CARD, found.getPaymentMethod());
    }

    @Test
    void testUpdatePaymentStatus() {
        Payment payment = new Payment();
        payment.setOrderId(5L);
        payment.setAmount(new BigDecimal("200.00"));
        payment.setPaymentMethod(PaymentMethod.WECHAT);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionNo("TXN20260209005");
        paymentService.addPayment(payment);

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());
        assertTrue(paymentService.updatePayment(payment));

        Payment updated = paymentService.getPaymentById(payment.getId());
        assertEquals(PaymentStatus.SUCCESS, updated.getPaymentStatus());
    }

    @Test
    void testDeletePayment() {
        Payment payment = new Payment();
        payment.setOrderId(6L);
        payment.setAmount(new BigDecimal("15.00"));
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionNo("TXN20260209006");
        paymentService.addPayment(payment);

        assertTrue(paymentService.deletePayment(payment.getId()));
        assertNull(paymentService.getPaymentById(payment.getId()));
    }

    @Test
    void testDeleteBatchPayments() {
        Payment p1 = new Payment();
        p1.setOrderId(7L);
        p1.setAmount(new BigDecimal("10.00"));
        p1.setPaymentMethod(PaymentMethod.ALIPAY);
        p1.setPaymentStatus(PaymentStatus.PENDING);
        p1.setTransactionNo("TXN20260209007");
        paymentService.addPayment(p1);

        Payment p2 = new Payment();
        p2.setOrderId(8L);
        p2.setAmount(new BigDecimal("20.00"));
        p2.setPaymentMethod(PaymentMethod.WECHAT);
        p2.setPaymentStatus(PaymentStatus.PENDING);
        p2.setTransactionNo("TXN20260209008");
        paymentService.addPayment(p2);

        assertTrue(paymentService.deleteBatchPayments(List.of(p1.getId(), p2.getId())));
        assertNull(paymentService.getPaymentById(p1.getId()));
        assertNull(paymentService.getPaymentById(p2.getId()));
    }
}
