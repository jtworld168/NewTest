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

    private Long createTestOrder(String suffix) {
        User user = new User();
        user.setUsername("oi_user_" + suffix + "_" + System.nanoTime());
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        Product product = new Product();
        product.setName("oi_prod_" + suffix + "_" + System.nanoTime());
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(100);
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

    private Long createTestProduct(String suffix) {
        Product product = new Product();
        product.setName("oi_item_" + suffix + "_" + System.nanoTime());
        product.setPrice(new BigDecimal("25.00"));
        product.setStock(200);
        productService.addProduct(product);
        return product.getId();
    }

    @Test
    void testAddAndGetOrderItem() {
        Long orderId = createTestOrder("add");
        Long productId = createTestProduct("add");

        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(3);
        item.setPriceAtPurchase(new BigDecimal("25.00"));
        item.setSubtotal(new BigDecimal("75.00"));

        assertTrue(orderItemService.addOrderItem(item));
        assertNotNull(item.getId());

        OrderItem found = orderItemService.getOrderItemById(item.getId());
        assertNotNull(found);
        assertEquals(orderId, found.getOrderId());
        assertEquals(productId, found.getProductId());
        assertEquals(3, found.getQuantity());
        assertEquals(new BigDecimal("25.00"), found.getPriceAtPurchase());
        assertEquals(new BigDecimal("75.00"), found.getSubtotal());
    }

    @Test
    void testGetOrderItemsByOrderId() {
        Long orderId = createTestOrder("byorder");
        Long productId1 = createTestProduct("byorder1");
        Long productId2 = createTestProduct("byorder2");

        OrderItem item1 = new OrderItem();
        item1.setOrderId(orderId);
        item1.setProductId(productId1);
        item1.setQuantity(2);
        item1.setPriceAtPurchase(new BigDecimal("25.00"));
        item1.setSubtotal(new BigDecimal("50.00"));
        orderItemService.addOrderItem(item1);

        OrderItem item2 = new OrderItem();
        item2.setOrderId(orderId);
        item2.setProductId(productId2);
        item2.setQuantity(1);
        item2.setPriceAtPurchase(new BigDecimal("25.00"));
        item2.setSubtotal(new BigDecimal("25.00"));
        orderItemService.addOrderItem(item2);

        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(orderId);
        assertEquals(2, items.size());
    }

    @Test
    void testGetOrderItemsByProductId() {
        Long orderId = createTestOrder("byprod");
        Long productId = createTestProduct("byprod");

        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(5);
        item.setPriceAtPurchase(new BigDecimal("25.00"));
        item.setSubtotal(new BigDecimal("125.00"));
        orderItemService.addOrderItem(item);

        List<OrderItem> items = orderItemService.getOrderItemsByProductId(productId);
        assertFalse(items.isEmpty());
        assertEquals(productId, items.get(0).getProductId());
    }

    @Test
    void testUpdateOrderItem() {
        Long orderId = createTestOrder("update");
        Long productId = createTestProduct("update");

        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(1);
        item.setPriceAtPurchase(new BigDecimal("25.00"));
        item.setSubtotal(new BigDecimal("25.00"));
        orderItemService.addOrderItem(item);

        item.setQuantity(4);
        item.setSubtotal(new BigDecimal("100.00"));
        assertTrue(orderItemService.updateOrderItem(item));

        OrderItem updated = orderItemService.getOrderItemById(item.getId());
        assertEquals(4, updated.getQuantity());
        assertEquals(new BigDecimal("100.00"), updated.getSubtotal());
    }

    @Test
    void testDeleteOrderItem() {
        Long orderId = createTestOrder("delete");
        Long productId = createTestProduct("delete");

        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(1);
        item.setPriceAtPurchase(new BigDecimal("25.00"));
        item.setSubtotal(new BigDecimal("25.00"));
        orderItemService.addOrderItem(item);

        assertTrue(orderItemService.deleteOrderItem(item.getId()));
        assertNull(orderItemService.getOrderItemById(item.getId()));
    }

    @Test
    void testDeleteBatchOrderItems() {
        Long orderId = createTestOrder("batch");
        Long productId1 = createTestProduct("batch1");
        Long productId2 = createTestProduct("batch2");

        OrderItem i1 = new OrderItem();
        i1.setOrderId(orderId);
        i1.setProductId(productId1);
        i1.setQuantity(1);
        i1.setPriceAtPurchase(new BigDecimal("25.00"));
        i1.setSubtotal(new BigDecimal("25.00"));
        orderItemService.addOrderItem(i1);

        OrderItem i2 = new OrderItem();
        i2.setOrderId(orderId);
        i2.setProductId(productId2);
        i2.setQuantity(2);
        i2.setPriceAtPurchase(new BigDecimal("25.00"));
        i2.setSubtotal(new BigDecimal("50.00"));
        orderItemService.addOrderItem(i2);

        assertTrue(orderItemService.deleteBatchOrderItems(List.of(i1.getId(), i2.getId())));
        assertNull(orderItemService.getOrderItemById(i1.getId()));
        assertNull(orderItemService.getOrderItemById(i2.getId()));
    }
}
