package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.CartItem;
import com.supermarket.mapper.CartItemMapper;
import com.supermarket.service.CartItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

    @Override
    public CartItem getCartItemById(Long id) {
        return getById(id);
    }

    @Override
    public List<CartItem> getCartItemsByUserId(Long userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
               .orderByDesc(CartItem::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<CartItem> getCartItemsByProductId(Long productId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getProductId, productId)
               .orderByDesc(CartItem::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<CartItem> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CartItem::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addCartItem(CartItem cartItem) {
        return save(cartItem);
    }

    @Override
    public boolean updateCartItem(CartItem cartItem) {
        return updateById(cartItem);
    }

    @Override
    public boolean deleteCartItem(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchCartItems(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public boolean clearCart(Long userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        return remove(wrapper);
    }

    @Override
    public List<CartItem> listAll() {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CartItem::getCreateTime);
        return list(wrapper);
    }
}
