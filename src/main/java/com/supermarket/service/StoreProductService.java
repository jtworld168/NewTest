package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.StoreProduct;

import java.util.List;

public interface StoreProductService extends IService<StoreProduct> {
    StoreProduct getStoreProductById(Long id);
    List<StoreProduct> getByStoreId(Long storeId);
    List<StoreProduct> getByProductId(Long productId);
    IPage<StoreProduct> listPage(int pageNum, int pageSize);
    IPage<StoreProduct> listPageByStoreId(Long storeId, int pageNum, int pageSize);
    boolean addStoreProduct(StoreProduct storeProduct);
    boolean updateStoreProduct(StoreProduct storeProduct);
    boolean deleteStoreProduct(Long id);
    boolean deleteBatchStoreProducts(List<Long> ids);
    List<StoreProduct> listAll();
    IPage<StoreProduct> searchByProductName(String productName, Long storeId, int pageNum, int pageSize);
    List<StoreProduct> searchByProductNameList(String productName, Long storeId);
    List<StoreProduct> getLowStockByStoreId(Long storeId, int threshold);
    List<StoreProduct> getLowStockByStoreIdUsingSafetyStock(Long storeId);
    void syncProductTotalStock(Long productId);
    StoreProduct addStoreProductWithName(String productName, StoreProduct storeProduct);
    StoreProduct getByStoreIdAndProductId(Long storeId, Long productId);
    /**
     * 扣减店铺商品库存，并同步总商品库存
     * @param storeId 店铺ID
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    boolean deductStoreStock(Long storeId, Long productId, int quantity);
}
