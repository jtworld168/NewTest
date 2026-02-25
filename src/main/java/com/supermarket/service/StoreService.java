package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Store;

import java.util.List;

public interface StoreService extends IService<Store> {
    Store getStoreById(Long id);
    Store getStoreByName(String name);
    List<Store> searchStores(String keyword);
    IPage<Store> listPage(int pageNum, int pageSize);
    boolean addStore(Store store);
    boolean updateStore(Store store);
    boolean deleteStore(Long id);
    boolean deleteBatchStores(List<Long> ids);
    List<Store> listAll();
}
