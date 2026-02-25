package com.supermarket;

import com.supermarket.entity.Order;
import com.supermarket.entity.Payment;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.enums.PaymentMethod;
import com.supermarket.enums.PaymentStatus;
import com.supermarket.enums.OrderStatus;
import com.supermarket.enums.UserRole;
import com.supermarket.service.OrderService;
import com.supermarket.service.PaymentService;
import com.supermarket.service.ProductService;
import com.supermarket.service.UserService;
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

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    private Long createTestOrder(String userSuffix) {
        User user = new User();
        user.setUsername("pay_user_" + userSuffix + "_" + System.nanoTime());
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        Product product = new Product();
        product.setName("pay_prod_" + userSuffix + "_" + System.nanoTime());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(50);
        productService.addProduct(product);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setProductId(product.getId());
        order.setQuantity(1);
        order.setPriceAtPurchase(product.getPrice());
        order.setTotalAmount(product.getPrice());
        order.setStatus(OrderStatus.PENDING);
        orderService.save(order);
        return order.getId();
    }

    @Test
    void testAddAndGetPayment() {
        Long orderId = createTestOrder("add");

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(new BigDecimal("99.90"));
        payment.setPaymentMethod(PaymentMethod.WECHAT);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionNo("TXN_ADD_" + System.nanoTime());

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
        Long orderId = createTestOrder("byorder");

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(new BigDecimal("50.00"));
        payment.setPaymentMethod(PaymentMethod.ALIPAY);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTransactionNo("TXN_ORDER_" + System.nanoTime());
        paymentService.addPayment(payment);

        Payment found = paymentService.getPaymentByOrderId(orderId);
        assertNotNull(found);
        assertEquals(PaymentMethod.ALIPAY, found.getPaymentMethod());
    }

    @Test
    void testGetPaymentsByStatus() {
        Long orderId = createTestOrder("status");

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(new BigDecimal("30.00"));
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setTransactionNo("TXN_STATUS_" + System.nanoTime());
        paymentService.addPayment(payment);

        List<Payment> failed = paymentService.getPaymentsByStatus(PaymentStatus.FAILED);
        assertFalse(failed.isEmpty());
    }

    @Test
    void testGetPaymentByTransactionNo() {
        Long orderId = createTestOrder("txn");
        String txnNo = "TXN_QUERY_" + System.nanoTime();

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(new BigDecimal("120.00"));
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionNo(txnNo);
        paymentService.addPayment(payment);

        Payment found = paymentService.getPaymentByTransactionNo(txnNo);
        assertNotNull(found);
        assertEquals(PaymentMethod.CARD, found.getPaymentMethod());
    }

    @Test
    void testUpdatePaymentStatus() {
        Long orderId = createTestOrder("update");

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(new BigDecimal("200.00"));
        payment.setPaymentMethod(PaymentMethod.WECHAT);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionNo("TXN_UPD_" + System.nanoTime());
        paymentService.addPayment(payment);

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());
        assertTrue(paymentService.updatePayment(payment));

        Payment updated = paymentService.getPaymentById(payment.getId());
        assertEquals(PaymentStatus.SUCCESS, updated.getPaymentStatus());
    }

    @Test
    void testDeletePayment() {
        Long orderId = createTestOrder("delete");

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(new BigDecimal("15.00"));
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionNo("TXN_DEL_" + System.nanoTime());
        paymentService.addPayment(payment);

        assertTrue(paymentService.deletePayment(payment.getId()));
        assertNull(paymentService.getPaymentById(payment.getId()));
    }

    @Test
    void testDeleteBatchPayments() {
        Long orderId1 = createTestOrder("batch1");
        Long orderId2 = createTestOrder("batch2");

        Payment p1 = new Payment();
        p1.setOrderId(orderId1);
        p1.setAmount(new BigDecimal("10.00"));
        p1.setPaymentMethod(PaymentMethod.ALIPAY);
        p1.setPaymentStatus(PaymentStatus.PENDING);
        p1.setTransactionNo("TXN_BATCH1_" + System.nanoTime());
        paymentService.addPayment(p1);

        Payment p2 = new Payment();
        p2.setOrderId(orderId2);
        p2.setAmount(new BigDecimal("20.00"));
        p2.setPaymentMethod(PaymentMethod.WECHAT);
        p2.setPaymentStatus(PaymentStatus.PENDING);
        p2.setTransactionNo("TXN_BATCH2_" + System.nanoTime());
        paymentService.addPayment(p2);

        assertTrue(paymentService.deleteBatchPayments(List.of(p1.getId(), p2.getId())));
        assertNull(paymentService.getPaymentById(p1.getId()));
        assertNull(paymentService.getPaymentById(p2.getId()));
    }
}
