package com.supermarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    Category getCategoryById(Long id);

    Category getCategoryByName(String name);

    boolean addCategory(Category category);

    boolean updateCategory(Category category);

    boolean deleteCategory(Long id);

    boolean deleteBatchCategories(List<Long> ids);
}
