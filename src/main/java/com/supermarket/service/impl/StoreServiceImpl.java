package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Store;
import com.supermarket.mapper.StoreMapper;
import com.supermarket.service.StoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    @Override
    public Store getStoreById(Long id) {
        return getById(id);
    }

    @Override
    public Store getStoreByName(String name) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getName, name);
        return getOne(wrapper);
    }

    @Override
    public List<Store> searchStores(String keyword) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Store::getName, keyword)
               .or().like(Store::getAddress, keyword)
               .orderByDesc(Store::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<Store> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Store::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addStore(Store store) {
        return save(store);
    }

    @Override
    public boolean updateStore(Store store) {
        return updateById(store);
    }

    @Override
    public boolean deleteStore(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchStores(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<Store> listAll() {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Store::getCreateTime);
        return list(wrapper);
    }
}
