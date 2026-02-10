package com.supermarket;

import com.supermarket.entity.Order;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.enums.OrderStatus;
import com.supermarket.enums.UserRole;
import com.supermarket.service.OrderService;
import com.supermarket.service.ProductService;
import com.supermarket.service.UserService;
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

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);
        return user;
    }

    private Product createTestProduct(String name, BigDecimal price, BigDecimal discountRate) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(100);
        product.setEmployeeDiscountRate(discountRate);
        productService.addProduct(product);
        return product;
    }

    @Test
    void testAddAndGetOrder() {
        User user = createTestUser("order_test_user1");
        Product product = createTestProduct("测试商品1", new BigDecimal("99.90"), null);

        assertTrue(orderService.addOrder(user.getId(), product.getId(), 1, null) != null);

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        assertFalse(orders.isEmpty());

        Order found = orders.get(0);
        assertNotNull(found.getId());
        assertEquals(user.getId(), found.getUserId());
        assertEquals(product.getId(), found.getProductId());
        assertEquals(1, found.getQuantity());
        assertEquals(0, new BigDecimal("99.90").compareTo(found.getPriceAtPurchase()));
        assertEquals(0, new BigDecimal("99.90").compareTo(found.getTotalAmount()));
        assertEquals(OrderStatus.PENDING, found.getStatus());
    }

    @Test
    void testGetOrdersByUserId() {
        User user = createTestUser("order_test_user2");
        Product product = createTestProduct("测试商品2", new BigDecimal("10.00"), null);

        orderService.addOrder(user.getId(), product.getId(), 1, null);
        orderService.addOrder(user.getId(), product.getId(), 2, null);

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        assertTrue(orders.size() >= 2);
    }

    @Test
    void testGetOrdersByStatus() {
        User user = createTestUser("order_test_user3");
        Product product = createTestProduct("测试商品3", new BigDecimal("50.00"), null);

        orderService.addOrder(user.getId(), product.getId(), 1, null);

        List<Order> orders = orderService.getOrdersByStatus(OrderStatus.PENDING);
        assertTrue(orders.size() >= 1);
    }

    @Test
    void testUpdateOrder() {
        User user = createTestUser("order_test_user4");
        Product product = createTestProduct("测试商品4", new BigDecimal("30.00"), null);

        orderService.addOrder(user.getId(), product.getId(), 1, null);

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        Order order = orders.get(0);
        order.setStatus(OrderStatus.PAID);
        assertTrue(orderService.updateOrder(order));

        Order updated = orderService.getOrderById(order.getId());
        assertEquals(OrderStatus.PAID, updated.getStatus());
    }

    @Test
    void testDeleteOrder() {
        User user = createTestUser("order_test_user5");
        Product product = createTestProduct("测试商品5", new BigDecimal("15.00"), null);

        orderService.addOrder(user.getId(), product.getId(), 1, null);

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        Long orderId = orders.get(0).getId();

        assertTrue(orderService.deleteOrder(orderId));

        Order deleted = orderService.getOrderById(orderId);
        assertNull(deleted);
    }

    @Test
    void testAddOrderWithEmployeeDiscount() {
        // Create a hotel employee user
        User employee = new User();
        employee.setUsername("hotel_emp_order_test");
        employee.setPassword("pass");
        employee.setRole(UserRole.EMPLOYEE);
        employee.setIsHotelEmployee(true);
        userService.addUser(employee);

        // Create a product with employee discount rate (80% = 0.80)
        Product product = createTestProduct("员工折扣商品", new BigDecimal("100.00"), new BigDecimal("0.80"));

        // Add order — should apply employee discount
        assertNotNull(orderService.addOrder(employee.getId(), product.getId(), 2, null));

        List<Order> orders = orderService.getOrdersByUserId(employee.getId());
        assertEquals(1, orders.size());

        Order order = orders.get(0);
        assertEquals(2, order.getQuantity());
        // unit price should be 100.00 * 0.80 = 80.00
        assertEquals(0, new BigDecimal("80.00").compareTo(order.getPriceAtPurchase()));
        // total should be 80.00 * 2 = 160.00
        assertEquals(0, new BigDecimal("160.00").compareTo(order.getTotalAmount()));
    }

    @Test
    void testAddOrderWithoutEmployeeDiscount() {
        // Create a normal customer (not hotel employee)
        User customer = new User();
        customer.setUsername("normal_customer_order_test");
        customer.setPassword("pass");
        customer.setRole(UserRole.CUSTOMER);
        customer.setIsHotelEmployee(false);
        userService.addUser(customer);

        // Create a product with employee discount rate
        Product product = createTestProduct("普通商品测试", new BigDecimal("50.00"), new BigDecimal("0.90"));

        // Add order — should NOT apply employee discount
        assertNotNull(orderService.addOrder(customer.getId(), product.getId(), 1, null));

        List<Order> orders = orderService.getOrdersByUserId(customer.getId());
        assertEquals(1, orders.size());

        // price should remain 50.00 (no discount for non-employee)
        assertEquals(0, new BigDecimal("50.00").compareTo(orders.get(0).getPriceAtPurchase()));
        assertEquals(0, new BigDecimal("50.00").compareTo(orders.get(0).getTotalAmount()));
    }

    @Test
    void testAddOrderNoDiscountRate() {
        // Create a hotel employee
        User employee = new User();
        employee.setUsername("emp_no_discount_order_test");
        employee.setPassword("pass");
        employee.setRole(UserRole.EMPLOYEE);
        employee.setIsHotelEmployee(true);
        userService.addUser(employee);

        // Create a product WITHOUT employee discount rate
        Product product = createTestProduct("无折扣商品测试", new BigDecimal("200.00"), null);

        // Add order — no discount rate on product, so full price
        assertNotNull(orderService.addOrder(employee.getId(), product.getId(), 1, null));

        List<Order> orders = orderService.getOrdersByUserId(employee.getId());
        assertEquals(1, orders.size());

        // price should remain 200.00 (no discount rate set)
        assertEquals(0, new BigDecimal("200.00").compareTo(orders.get(0).getPriceAtPurchase()));
    }

    @Test
    void testGetOrdersByProductId() {
        User user = createTestUser("order_product_test_user");
        Product product = createTestProduct("按商品查询测试", new BigDecimal("25.00"), null);

        orderService.addOrder(user.getId(), product.getId(), 3, null);

        List<Order> orders = orderService.getOrdersByProductId(product.getId());
        assertTrue(orders.size() >= 1);
        assertEquals(product.getId(), orders.get(0).getProductId());
    }

    @Test
    void testDeleteBatchOrders() {
        User user = createTestUser("batch_del_order_user");
        Product product = createTestProduct("批量删除订单商品", new BigDecimal("10.00"), null);

        orderService.addOrder(user.getId(), product.getId(), 1, null);
        orderService.addOrder(user.getId(), product.getId(), 2, null);

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        assertTrue(orders.size() >= 2);

        List<Long> ids = List.of(orders.get(0).getId(), orders.get(1).getId());
        assertTrue(orderService.deleteBatchOrders(ids));

        assertNull(orderService.getOrderById(ids.get(0)));
        assertNull(orderService.getOrderById(ids.get(1)));
    }
}
