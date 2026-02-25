package com.supermarket;

import com.supermarket.entity.Category;
import com.supermarket.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    void testAddAndGetCategory() {
        Category category = new Category();
        category.setName("饮料");
        category.setDescription("各种饮品");

        assertTrue(categoryService.addCategory(category));
        assertNotNull(category.getId());

        Category found = categoryService.getCategoryById(category.getId());
        assertNotNull(found);
        assertEquals("饮料", found.getName());
        assertEquals("各种饮品", found.getDescription());
    }

    @Test
    void testGetCategoryByName() {
        Category category = new Category();
        category.setName("零食");
        category.setDescription("各种零食");
        categoryService.addCategory(category);

        Category found = categoryService.getCategoryByName("零食");
        assertNotNull(found);
        assertEquals("各种零食", found.getDescription());
    }

    @Test
    void testUpdateCategory() {
        Category category = new Category();
        category.setName("日用品");
        category.setDescription("日常用品");
        categoryService.addCategory(category);

        category.setDescription("家居日用品");
        assertTrue(categoryService.updateCategory(category));

        Category updated = categoryService.getCategoryById(category.getId());
        assertEquals("家居日用品", updated.getDescription());
    }

    @Test
    void testDeleteCategory() {
        Category category = new Category();
        category.setName("临时分类");
        categoryService.addCategory(category);

        assertTrue(categoryService.deleteCategory(category.getId()));

        Category deleted = categoryService.getCategoryById(category.getId());
        assertNull(deleted);
    }

    @Test
    void testDeleteBatchCategories() {
        Category c1 = new Category();
        c1.setName("批量删除分类1");
        categoryService.addCategory(c1);

        Category c2 = new Category();
        c2.setName("批量删除分类2");
        categoryService.addCategory(c2);

        assertTrue(categoryService.deleteBatchCategories(List.of(c1.getId(), c2.getId())));

        assertNull(categoryService.getCategoryById(c1.getId()));
        assertNull(categoryService.getCategoryById(c2.getId()));
    }

    @Test
    void testListAllCategories() {
        long countBefore = categoryService.list().size();

        Category category = new Category();
        category.setName("测试列表分类");
        categoryService.addCategory(category);

        List<Category> all = categoryService.list();
        assertTrue(all.size() > countBefore);
    }
}
