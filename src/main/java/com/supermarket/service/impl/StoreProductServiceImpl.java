package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.StoreProduct;
import com.supermarket.mapper.StoreProductMapper;
import com.supermarket.service.StoreProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreProductServiceImpl extends ServiceImpl<StoreProductMapper, StoreProduct> implements StoreProductService {

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
        return save(storeProduct);
    }

    @Override
    public boolean updateStoreProduct(StoreProduct storeProduct) {
        return updateById(storeProduct);
    }

    @Override
    public boolean deleteStoreProduct(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchStoreProducts(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<StoreProduct> listAll() {
        LambdaQueryWrapper<StoreProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StoreProduct::getCreateTime);
        return list(wrapper);
    }
}
