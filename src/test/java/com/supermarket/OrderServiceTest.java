package com.supermarket;

import com.supermarket.entity.Order;
import com.supermarket.enums.OrderStatus;
import com.supermarket.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void testAddAndGetOrder() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("99.90"));
        order.setStatus(OrderStatus.PENDING);

        assertTrue(orderService.addOrder(order));
        assertNotNull(order.getId());

        Order found = orderService.getOrderById(order.getId());
        assertNotNull(found);
        assertEquals(1L, found.getUserId());
        assertEquals(0, new BigDecimal("99.90").compareTo(found.getTotalAmount()));
        assertEquals(OrderStatus.PENDING, found.getStatus());
    }

    @Test
    void testGetOrdersByUserId() {
        Order o1 = new Order();
        o1.setUserId(100L);
        o1.setTotalAmount(new BigDecimal("10.00"));
        o1.setStatus(OrderStatus.PENDING);
        orderService.addOrder(o1);

        Order o2 = new Order();
        o2.setUserId(100L);
        o2.setTotalAmount(new BigDecimal("20.00"));
        o2.setStatus(OrderStatus.PAID);
        orderService.addOrder(o2);

        List<Order> orders = orderService.getOrdersByUserId(100L);
        assertTrue(orders.size() >= 2);
    }

    @Test
    void testGetOrdersByStatus() {
        Order order = new Order();
        order.setUserId(2L);
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setStatus(OrderStatus.COMPLETED);
        orderService.addOrder(order);

        List<Order> orders = orderService.getOrdersByStatus(OrderStatus.COMPLETED);
        assertTrue(orders.size() >= 1);
    }

    @Test
    void testUpdateOrder() {
        Order order = new Order();
        order.setUserId(3L);
        order.setTotalAmount(new BigDecimal("30.00"));
        order.setStatus(OrderStatus.PENDING);
        orderService.addOrder(order);

        order.setStatus(OrderStatus.PAID);
        assertTrue(orderService.updateOrder(order));

        Order updated = orderService.getOrderById(order.getId());
        assertEquals(OrderStatus.PAID, updated.getStatus());
    }

    @Test
    void testDeleteOrder() {
        Order order = new Order();
        order.setUserId(4L);
        order.setTotalAmount(new BigDecimal("15.00"));
        order.setStatus(OrderStatus.CANCELLED);
        orderService.addOrder(order);

        assertTrue(orderService.deleteOrder(order.getId()));

        Order deleted = orderService.getOrderById(order.getId());
        assertNull(deleted);
    }
}
