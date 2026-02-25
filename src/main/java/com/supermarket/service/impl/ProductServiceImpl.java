package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Product;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final int DEFAULT_STOCK_ALERT_THRESHOLD = 10;
    private static final String CACHE_PRODUCT_ALL = "cache:product:all";
    private static final String CACHE_PRODUCT_ID = "cache:product:id:";
    private static final String CACHE_PRODUCT_ON_SHELF = "cache:product:onShelf";
    private static final long CACHE_TTL_MINUTES = 10;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Product getProductById(Long id) {
        if (redisTemplate != null) {
            @SuppressWarnings("unchecked")
            Product cached = (Product) redisTemplate.opsForValue().get(CACHE_PRODUCT_ID + id);
            if (cached != null) return cached;
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, id);
        Product product = getOne(wrapper);
        if (product != null && redisTemplate != null) {
            redisTemplate.opsForValue().set(CACHE_PRODUCT_ID + id, product, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return product;
    }

    @Override
    public Product getProductByName(String name) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getName, name);
        return getOne(wrapper);
    }

    @Override
    public Product getProductByBarcode(String barcode) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getBarcode, barcode);
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
        boolean result = save(product);
        if (result) evictProductCache();
        return result;
    }

    @Override
    public boolean updateProduct(Product product) {
        boolean result = updateById(product);
        if (result) {
            evictProductCache();
            if (product.getId() != null && redisTemplate != null) {
                redisTemplate.delete(CACHE_PRODUCT_ID + product.getId());
            }
        }
        return result;
    }

    @Override
    public boolean deleteProduct(Long id) {
        boolean result = removeById(id);
        if (result) {
            evictProductCache();
            if (redisTemplate != null) {
                redisTemplate.delete(CACHE_PRODUCT_ID + id);
            }
        }
        return result;
    }

    @Override
    public boolean deleteBatchProducts(List<Long> ids) {
        boolean result = removeByIds(ids);
        if (result) evictProductCache();
        return result;
    }

    private void evictProductCache() {
        if (redisTemplate != null) {
            redisTemplate.delete(CACHE_PRODUCT_ALL);
            redisTemplate.delete(CACHE_PRODUCT_ON_SHELF);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Product> listAll() {
        if (redisTemplate != null) {
            List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(CACHE_PRODUCT_ALL);
            if (cached != null) return cached;
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreateTime);
        List<Product> products = list(wrapper);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(CACHE_PRODUCT_ALL, products, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return products;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Product> getOnShelfProducts() {
        if (redisTemplate != null) {
            List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(CACHE_PRODUCT_ON_SHELF);
            if (cached != null) return cached;
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .orderByDesc(Product::getCreateTime);
        List<Product> products = list(wrapper);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(CACHE_PRODUCT_ON_SHELF, products, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return products;
    }

    @Override
    public List<Product> getLowStockProducts() {
        return list(new LambdaQueryWrapper<Product>()
            .apply("stock <= COALESCE(stock_alert_threshold, {0})", DEFAULT_STOCK_ALERT_THRESHOLD)
            .eq(Product::getStatus, 1)
            .orderByAsc(Product::getStock));
    }
}
