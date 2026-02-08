package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Product;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Product getProductById(Long id) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, id);
        return getOne(wrapper);
    }

    @Override
    public Product getProductByName(String name) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getName, name);
        return getOne(wrapper);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getName, name);
        return list(wrapper);
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId);
        return list(wrapper);
    }

    @Override
    public boolean addProduct(Product product) {
        return save(product);
    }

    @Override
    public boolean updateProduct(Product product) {
        return updateById(product);
    }

    @Override
    public boolean deleteProduct(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchProducts(List<Long> ids) {
        return removeByIds(ids);
    }
}
