package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.StoreProduct;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoreProductMapper extends BaseMapper<StoreProduct> {
}
