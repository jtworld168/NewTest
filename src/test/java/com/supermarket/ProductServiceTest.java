package com.supermarket;

import com.supermarket.entity.Product;
import com.supermarket.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void testAddAndGetProduct() {
        Product product = new Product();
        product.setName("可乐");
        product.setPrice(new BigDecimal("3.50"));
        product.setStock(100);
        product.setDescription("冰镇可乐");

        assertTrue(productService.addProduct(product));
        assertNotNull(product.getId());

        Product found = productService.getProductById(product.getId());
        assertNotNull(found);
        assertEquals("可乐", found.getName());
        assertEquals(0, new BigDecimal("3.50").compareTo(found.getPrice()));
        assertEquals(100, found.getStock());
    }

    @Test
    void testGetProductByName() {
        Product product = new Product();
        product.setName("矿泉水");
        product.setPrice(new BigDecimal("2.00"));
        product.setStock(200);
        productService.addProduct(product);

        Product found = productService.getProductByName("矿泉水");
        assertNotNull(found);
        assertEquals(200, found.getStock());
    }

    @Test
    void testSearchProductsByName() {
        Product p1 = new Product();
        p1.setName("牛奶面包");
        p1.setPrice(new BigDecimal("5.00"));
        p1.setStock(50);
        productService.addProduct(p1);

        Product p2 = new Product();
        p2.setName("全麦面包");
        p2.setPrice(new BigDecimal("8.00"));
        p2.setStock(30);
        productService.addProduct(p2);

        List<Product> results = productService.searchProductsByName("面包");
        assertTrue(results.size() >= 2);
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product();
        product.setName("薯片");
        product.setPrice(new BigDecimal("6.00"));
        product.setStock(80);
        productService.addProduct(product);

        product.setPrice(new BigDecimal("7.50"));
        assertTrue(productService.updateProduct(product));

        Product updated = productService.getProductById(product.getId());
        assertEquals(0, new BigDecimal("7.50").compareTo(updated.getPrice()));
    }

    @Test
    void testDeleteProduct() {
        Product product = new Product();
        product.setName("饼干");
        product.setPrice(new BigDecimal("4.00"));
        product.setStock(60);
        productService.addProduct(product);

        assertTrue(productService.deleteProduct(product.getId()));

        Product deleted = productService.getProductById(product.getId());
        assertNull(deleted);
    }
}
