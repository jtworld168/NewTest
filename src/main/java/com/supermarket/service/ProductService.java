package com.supermarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {

    Product getProductById(Long id);

    Product getProductByName(String name);

    List<Product> searchProductsByName(String name);

    List<Product> getProductsByCategoryId(Long categoryId);

    boolean addProduct(Product product);

    boolean updateProduct(Product product);

    boolean deleteProduct(Long id);

    boolean deleteBatchProducts(List<Long> ids);
}
