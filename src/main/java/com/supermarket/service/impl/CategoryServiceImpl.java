package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Category;
import com.supermarket.mapper.CategoryMapper;
import com.supermarket.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Category getCategoryById(Long id) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getId, id);
        return getOne(wrapper);
    }

    @Override
    public Category getCategoryByName(String name) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name);
        return getOne(wrapper);
    }

    @Override
    public boolean addCategory(Category category) {
        return save(category);
    }

    @Override
    public boolean updateCategory(Category category) {
        return updateById(category);
    }

    @Override
    public boolean deleteCategory(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchCategories(List<Long> ids) {
        return removeByIds(ids);
    }
}
