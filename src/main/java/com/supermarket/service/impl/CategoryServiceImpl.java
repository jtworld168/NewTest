package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Category;
import com.supermarket.mapper.CategoryMapper;
import com.supermarket.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private static final String CACHE_CATEGORY_ALL = "cache:category:all";
    private static final String CACHE_CATEGORY_ID = "cache:category:id:";
    private static final long CACHE_TTL_MINUTES = 30;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Category getCategoryById(Long id) {
        if (redisTemplate != null) {
            @SuppressWarnings("unchecked")
            Category cached = (Category) redisTemplate.opsForValue().get(CACHE_CATEGORY_ID + id);
            if (cached != null) return cached;
        }
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getId, id);
        Category category = getOne(wrapper);
        if (category != null && redisTemplate != null) {
            redisTemplate.opsForValue().set(CACHE_CATEGORY_ID + id, category, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return category;
    }

    @Override
    public Category getCategoryByName(String name) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name);
        return getOne(wrapper);
    }

    @Override
    public List<Category> searchCategories(String keyword) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Category::getName, keyword)
                .or().like(Category::getDescription, keyword))
               .orderByDesc(Category::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<Category> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addCategory(Category category) {
        boolean result = save(category);
        if (result) evictCategoryCache();
        return result;
    }

    @Override
    public boolean updateCategory(Category category) {
        boolean result = updateById(category);
        if (result) {
            evictCategoryCache();
            if (category.getId() != null && redisTemplate != null) {
                redisTemplate.delete(CACHE_CATEGORY_ID + category.getId());
            }
        }
        return result;
    }

    @Override
    public boolean deleteCategory(Long id) {
        boolean result = removeById(id);
        if (result) {
            evictCategoryCache();
            if (redisTemplate != null) {
                redisTemplate.delete(CACHE_CATEGORY_ID + id);
            }
        }
        return result;
    }

    @Override
    public boolean deleteBatchCategories(List<Long> ids) {
        boolean result = removeByIds(ids);
        if (result) evictCategoryCache();
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Category> listAll() {
        if (redisTemplate != null) {
            List<Category> cached = (List<Category>) redisTemplate.opsForValue().get(CACHE_CATEGORY_ALL);
            if (cached != null) return cached;
        }
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getCreateTime);
        List<Category> categories = list(wrapper);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(CACHE_CATEGORY_ALL, categories, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return categories;
    }

    private void evictCategoryCache() {
        if (redisTemplate != null) {
            redisTemplate.delete(CACHE_CATEGORY_ALL);
        }
    }
}
