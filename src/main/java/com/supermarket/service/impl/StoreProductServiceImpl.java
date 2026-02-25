package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Product;
import com.supermarket.entity.StoreProduct;
import com.supermarket.mapper.StoreProductMapper;
import com.supermarket.service.ProductService;
import com.supermarket.service.StoreProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreProductServiceImpl extends ServiceImpl<StoreProductMapper, StoreProduct> implements StoreProductService {

    private static final int STATUS_ON_SHELF = 1;

    private final ProductService productService;

    @Override
    public StoreProduct getStoreProductById(Long id) {
        return getById(id);
    }

    @Override
    public List<StoreProduct> getByStoreId(Long storeId) {
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreProduct::getStoreId, storeId)
               .orderByDesc(StoreProduct::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<StoreProduct> getByProductId(Long productId) {
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreProduct::getProductId, productId)
               .orderByDesc(StoreProduct::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<StoreProduct> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StoreProduct::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public IPage<StoreProduct> listPageByStoreId(Long storeId, int pageNum, int pageSize) {
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreProduct::getStoreId, storeId)
               .orderByDesc(StoreProduct::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addStoreProduct(StoreProduct storeProduct) {
        boolean result = save(storeProduct);
        if (result && storeProduct.getProductId() != null) {
            syncProductTotalStock(storeProduct.getProductId());
        }
        return result;
    }

    @Override
    public boolean updateStoreProduct(StoreProduct storeProduct) {
        boolean result = updateById(storeProduct);
        if (result && storeProduct.getProductId() != null) {
            syncProductTotalStock(storeProduct.getProductId());
        }
        return result;
    }

    @Override
    public boolean deleteStoreProduct(Long id) {
        StoreProduct sp = getById(id);
        boolean result = removeById(id);
        if (result && sp != null && sp.getProductId() != null) {
            syncProductTotalStock(sp.getProductId());
        }
        return result;
    }

    @Override
    public boolean deleteBatchStoreProducts(List<Long> ids) {
        List<StoreProduct> items = listByIds(ids);
        boolean result = removeByIds(ids);
        if (result) {
            items.stream()
                 .map(StoreProduct::getProductId)
                 .distinct()
                 .forEach(this::syncProductTotalStock);
        }
        return result;
    }

    @Override
    public void syncProductTotalStock(Long productId) {
        if (productId == null) return;
        List<StoreProduct> storeProducts = getByProductId(productId);
        int totalStock = storeProducts.stream()
                .mapToInt(sp -> sp.getStoreStock() != null ? sp.getStoreStock() : 0)
                .sum();
        Product product = productService.getProductById(productId);
        if (product != null) {
            product.setStock(totalStock);
            productService.updateProduct(product);
        }
    }

    @Override
    @Transactional
    public StoreProduct addStoreProductWithName(String productName, StoreProduct storeProduct) {
        Product existingProduct = productService.getProductByName(productName);
        if (existingProduct != null) {
            storeProduct.setProductId(existingProduct.getId());
            if (storeProduct.getStorePrice() == null) {
                storeProduct.setStorePrice(existingProduct.getPrice());
            }
        } else {
            Product newProduct = new Product();
            newProduct.setName(productName);
            newProduct.setPrice(storeProduct.getStorePrice() != null ? storeProduct.getStorePrice() : BigDecimal.ZERO);
            newProduct.setStock(storeProduct.getStoreStock() != null ? storeProduct.getStoreStock() : 0);
            newProduct.setStatus(STATUS_ON_SHELF);
            productService.addProduct(newProduct);
            storeProduct.setProductId(newProduct.getId());
        }
        save(storeProduct);
        syncProductTotalStock(storeProduct.getProductId());
        return storeProduct;
    }

    @Override
    public List<StoreProduct> listAll() {
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StoreProduct::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<StoreProduct> searchByProductName(String productName, Long storeId, int pageNum, int pageSize) {
        // Find product IDs matching the name (select ID only)
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.like(Product::getName, productName)
                      .select(Product::getId);
        List<Long> productIds = productService.list(productWrapper).stream()
                .map(Product::getId).collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(StoreProduct::getProductId, productIds);
        if (storeId != null) {
            wrapper.eq(StoreProduct::getStoreId, storeId);
        }
        wrapper.orderByDesc(StoreProduct::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<StoreProduct> getLowStockByStoreId(Long storeId, int threshold) {
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreProduct::getStoreId, storeId)
               .lt(StoreProduct::getStoreStock, threshold)
               .orderByAsc(StoreProduct::getStoreStock);
        return list(wrapper);
    }

    @Override
    public List<StoreProduct> getLowStockByStoreIdUsingSafetyStock(Long storeId) {
        List<StoreProduct> storeProducts = getByStoreId(storeId);
        return storeProducts.stream()
                .filter(sp -> {
                    int safetyStock = sp.getSafetyStock() != null ? sp.getSafetyStock() : 10;
                    int currentStock = sp.getStoreStock() != null ? sp.getStoreStock() : 0;
                    return currentStock < safetyStock;
                })
                .collect(Collectors.toList());
    }
}
