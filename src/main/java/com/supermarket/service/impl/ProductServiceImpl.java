package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Product;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final int DEFAULT_STOCK_ALERT_THRESHOLD = 10;

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
        wrapper.like(Product::getName, name)
               .orderByDesc(Product::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Product::getName, keyword)
                .or().like(Product::getDescription, keyword))
               .orderByDesc(Product::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<Product> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
               .orderByDesc(Product::getCreateTime);
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

    @Override
    public List<Product> listAll() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Product> getOnShelfProducts() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .orderByDesc(Product::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Product> getLowStockProducts() {
        return list(new LambdaQueryWrapper<Product>()
            .apply("stock <= COALESCE(stock_alert_threshold, {0})", DEFAULT_STOCK_ALERT_THRESHOLD)
            .eq(Product::getStatus, 1)
            .orderByAsc(Product::getStock));
    }
}
