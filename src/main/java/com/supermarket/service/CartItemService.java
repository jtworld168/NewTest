package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.CartItem;

import java.util.List;

public interface CartItemService extends IService<CartItem> {

    CartItem getCartItemById(Long id);

    List<CartItem> getCartItemsByUserId(Long userId);

    List<CartItem> getCartItemsByProductId(Long productId);

    IPage<CartItem> listPage(int pageNum, int pageSize);

    boolean addCartItem(CartItem cartItem);

    boolean updateCartItem(CartItem cartItem);

    boolean deleteCartItem(Long id);

    boolean deleteBatchCartItems(List<Long> ids);

    boolean clearCart(Long userId);
}
