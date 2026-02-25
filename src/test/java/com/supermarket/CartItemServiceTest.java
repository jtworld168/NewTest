package com.supermarket;

import com.supermarket.entity.CartItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.CartItemService;
import com.supermarket.service.ProductService;
import com.supermarket.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartItemServiceTest {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    private Long createTestUser(String suffix) {
        User user = new User();
        user.setUsername("cart_user_" + suffix + "_" + System.nanoTime());
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);
        return user.getId();
    }

    private Long createTestProduct(String suffix) {
        Product product = new Product();
        product.setName("cart_prod_" + suffix + "_" + System.nanoTime());
        product.setPrice(new BigDecimal("30.00"));
        product.setStock(100);
        productService.addProduct(product);
        return product.getId();
    }

    @Test
    void testAddAndGetCartItem() {
        Long userId = createTestUser("add");
        Long productId = createTestProduct("add");

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(2);

        assertTrue(cartItemService.addCartItem(item));
        assertNotNull(item.getId());

        CartItem found = cartItemService.getCartItemById(item.getId());
        assertNotNull(found);
        assertEquals(userId, found.getUserId());
        assertEquals(productId, found.getProductId());
        assertEquals(2, found.getQuantity());
    }

    @Test
    void testGetCartItemsByUserId() {
        Long userId = createTestUser("byuser");
        Long productId1 = createTestProduct("byuser1");
        Long productId2 = createTestProduct("byuser2");

        CartItem item1 = new CartItem();
        item1.setUserId(userId);
        item1.setProductId(productId1);
        item1.setQuantity(1);
        cartItemService.addCartItem(item1);

        CartItem item2 = new CartItem();
        item2.setUserId(userId);
        item2.setProductId(productId2);
        item2.setQuantity(3);
        cartItemService.addCartItem(item2);

        List<CartItem> items = cartItemService.getCartItemsByUserId(userId);
        assertEquals(2, items.size());
    }

    @Test
    void testUpdateCartItem() {
        Long userId = createTestUser("update");
        Long productId = createTestProduct("update");

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(1);
        cartItemService.addCartItem(item);

        item.setQuantity(5);
        assertTrue(cartItemService.updateCartItem(item));

        CartItem updated = cartItemService.getCartItemById(item.getId());
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void testDeleteCartItem() {
        Long userId = createTestUser("delete");
        Long productId = createTestProduct("delete");

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(1);
        cartItemService.addCartItem(item);

        assertTrue(cartItemService.deleteCartItem(item.getId()));
        assertNull(cartItemService.getCartItemById(item.getId()));
    }

    @Test
    void testClearCart() {
        Long userId = createTestUser("clear");
        Long productId1 = createTestProduct("clear1");
        Long productId2 = createTestProduct("clear2");

        CartItem item1 = new CartItem();
        item1.setUserId(userId);
        item1.setProductId(productId1);
        item1.setQuantity(1);
        cartItemService.addCartItem(item1);

        CartItem item2 = new CartItem();
        item2.setUserId(userId);
        item2.setProductId(productId2);
        item2.setQuantity(2);
        cartItemService.addCartItem(item2);

        assertTrue(cartItemService.clearCart(userId));
        List<CartItem> items = cartItemService.getCartItemsByUserId(userId);
        assertTrue(items.isEmpty());
    }

    @Test
    void testDeleteBatchCartItems() {
        Long userId = createTestUser("batch");
        Long productId1 = createTestProduct("batch1");
        Long productId2 = createTestProduct("batch2");

        CartItem i1 = new CartItem();
        i1.setUserId(userId);
        i1.setProductId(productId1);
        i1.setQuantity(1);
        cartItemService.addCartItem(i1);

        CartItem i2 = new CartItem();
        i2.setUserId(userId);
        i2.setProductId(productId2);
        i2.setQuantity(2);
        cartItemService.addCartItem(i2);

        assertTrue(cartItemService.deleteBatchCartItems(List.of(i1.getId(), i2.getId())));
        assertNull(cartItemService.getCartItemById(i1.getId()));
        assertNull(cartItemService.getCartItemById(i2.getId()));
    }
}
