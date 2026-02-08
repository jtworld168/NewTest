package com.supermarket;

import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.enums.OrderStatus;
import com.supermarket.enums.UserRole;
import com.supermarket.service.OrderItemService;
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
class OrderItemServiceTest {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Test
    void testAddOrderItemWithEmployeeDiscount() {
        // Create a hotel employee user
        User employee = new User();
        employee.setUsername("hotel_emp_test");
        employee.setPassword("pass");
        employee.setRole(UserRole.EMPLOYEE);
        employee.setIsHotelEmployee(true);
        userService.addUser(employee);

        // Create a product with employee discount rate (80% = 0.80)
        Product product = new Product();
        product.setName("员工折扣商品");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(50);
        product.setEmployeeDiscountRate(new BigDecimal("0.80"));
        productService.addProduct(product);

        // Create an order for the employee
        Order order = new Order();
        order.setUserId(employee.getId());
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.PENDING);
        orderService.addOrder(order);

        // Add order item — should apply employee discount
        assertTrue(orderItemService.addOrderItem(order.getId(), product.getId(), 2));

        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());
        assertEquals(1, items.size());

        OrderItem item = items.get(0);
        assertEquals(2, item.getQuantity());
        // price should be 100.00 * 0.80 = 80.00
        assertEquals(0, new BigDecimal("80.00").compareTo(item.getPriceAtPurchase()));
    }

    @Test
    void testAddOrderItemWithoutEmployeeDiscount() {
        // Create a normal customer (not hotel employee)
        User customer = new User();
        customer.setUsername("normal_customer_test");
        customer.setPassword("pass");
        customer.setRole(UserRole.CUSTOMER);
        customer.setIsHotelEmployee(false);
        userService.addUser(customer);

        // Create a product with employee discount rate
        Product product = new Product();
        product.setName("普通商品");
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(30);
        product.setEmployeeDiscountRate(new BigDecimal("0.90"));
        productService.addProduct(product);

        // Create an order for the customer
        Order order = new Order();
        order.setUserId(customer.getId());
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setStatus(OrderStatus.PENDING);
        orderService.addOrder(order);

        // Add order item — should NOT apply employee discount
        assertTrue(orderItemService.addOrderItem(order.getId(), product.getId(), 1));

        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());
        assertEquals(1, items.size());

        OrderItem item = items.get(0);
        // price should remain 50.00 (no discount for non-employee)
        assertEquals(0, new BigDecimal("50.00").compareTo(item.getPriceAtPurchase()));
    }

    @Test
    void testAddOrderItemNoDiscountRate() {
        // Create a hotel employee
        User employee = new User();
        employee.setUsername("emp_no_discount_test");
        employee.setPassword("pass");
        employee.setRole(UserRole.EMPLOYEE);
        employee.setIsHotelEmployee(true);
        userService.addUser(employee);

        // Create a product WITHOUT employee discount rate
        Product product = new Product();
        product.setName("无折扣商品");
        product.setPrice(new BigDecimal("200.00"));
        product.setStock(10);
        // employeeDiscountRate is null
        productService.addProduct(product);

        // Create an order
        Order order = new Order();
        order.setUserId(employee.getId());
        order.setTotalAmount(new BigDecimal("200.00"));
        order.setStatus(OrderStatus.PENDING);
        orderService.addOrder(order);

        // Add order item — no discount rate on product, so full price
        assertTrue(orderItemService.addOrderItem(order.getId(), product.getId(), 1));

        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());
        assertEquals(1, items.size());

        // price should remain 200.00 (no discount rate set)
        assertEquals(0, new BigDecimal("200.00").compareTo(items.get(0).getPriceAtPurchase()));
    }

    @Test
    void testUpdateOrderItem() {
        User user = new User();
        user.setUsername("update_item_user");
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        Product product = new Product();
        product.setName("更新测试商品");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(100);
        productService.addProduct(product);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setStatus(OrderStatus.PENDING);
        orderService.addOrder(order);

        orderItemService.addOrderItem(order.getId(), product.getId(), 1);

        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());
        OrderItem item = items.get(0);
        item.setQuantity(5);
        assertTrue(orderItemService.updateOrderItem(item));

        OrderItem updated = orderItemService.getOrderItemById(item.getId());
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void testDeleteOrderItem() {
        User user = new User();
        user.setUsername("delete_item_user");
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        Product product = new Product();
        product.setName("删除测试商品");
        product.setPrice(new BigDecimal("20.00"));
        product.setStock(50);
        productService.addProduct(product);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setStatus(OrderStatus.PENDING);
        orderService.addOrder(order);

        orderItemService.addOrderItem(order.getId(), product.getId(), 1);

        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());
        Long itemId = items.get(0).getId();

        assertTrue(orderItemService.deleteOrderItem(itemId));

        OrderItem deleted = orderItemService.getOrderItemById(itemId);
        assertNull(deleted);
    }
}
