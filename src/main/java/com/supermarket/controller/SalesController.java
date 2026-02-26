package com.supermarket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.common.Result;
import com.supermarket.entity.Category;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.enums.OrderStatus;
import com.supermarket.entity.StoreProduct;
import com.supermarket.service.OrderService;
import com.supermarket.service.OrderItemService;
import com.supermarket.service.ProductService;
import com.supermarket.service.CategoryService;
import com.supermarket.service.StoreProductService;
import com.supermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "销量管理", description = "销量统计接口（总销量/店铺销量/按月份年份统计）")
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final StoreProductService storeProductService;
    private final UserService userService;

    @Operation(summary = "获取销量总览（按天统计）")
    @GetMapping("/summary/daily")
    public Result<List<Map<String, Object>>> getDailySales(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month,
            @Parameter(description = "店铺ID（可选，为空查全部）") @RequestParam(required = false) Long storeId) {
        List<Order> orders = getCompletedOrders(storeId);

        // Get days in month
        java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        Map<Integer, BigDecimal> dailyMap = new TreeMap<>();
        Map<Integer, Integer> dailyCountMap = new TreeMap<>();
        for (int i = 1; i <= daysInMonth; i++) {
            dailyMap.put(i, BigDecimal.ZERO);
            dailyCountMap.put(i, 0);
        }

        for (Order order : orders) {
            if (order.getCreateTime() != null
                    && order.getCreateTime().getYear() == year
                    && order.getCreateTime().getMonthValue() == month) {
                int day = order.getCreateTime().getDayOfMonth();
                dailyMap.merge(day, order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO, BigDecimal::add);
                dailyCountMap.merge(day, 1, Integer::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", i);
            item.put("dayLabel", month + "月" + i + "日");
            item.put("totalAmount", dailyMap.get(i));
            item.put("orderCount", dailyCountMap.get(i));
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取销量总览（按月统计）")
    @GetMapping("/summary/monthly")
    public Result<List<Map<String, Object>>> getMonthlySales(
            @Parameter(description = "年份（默认当前年）") @RequestParam(required = false) Integer year,
            @Parameter(description = "店铺ID（可选，为空查全部）") @RequestParam(required = false) Long storeId) {
        int targetYear = year != null ? year : LocalDateTime.now().getYear();
        List<Order> orders = getCompletedOrders(storeId);

        Map<Integer, BigDecimal> monthlyMap = new TreeMap<>();
        Map<Integer, Integer> monthlyCountMap = new TreeMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyMap.put(i, BigDecimal.ZERO);
            monthlyCountMap.put(i, 0);
        }

        for (Order order : orders) {
            if (order.getCreateTime() != null && order.getCreateTime().getYear() == targetYear) {
                int month = order.getCreateTime().getMonthValue();
                monthlyMap.merge(month, order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO, BigDecimal::add);
                monthlyCountMap.merge(month, 1, Integer::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", i);
            item.put("monthLabel", i + "月");
            item.put("totalAmount", monthlyMap.get(i));
            item.put("orderCount", monthlyCountMap.get(i));
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取销量总览（按年统计）")
    @GetMapping("/summary/yearly")
    public Result<List<Map<String, Object>>> getYearlySales(
            @Parameter(description = "店铺ID（可选，为空查全部）") @RequestParam(required = false) Long storeId) {
        List<Order> orders = getCompletedOrders(storeId);

        Map<Integer, BigDecimal> yearlyMap = new TreeMap<>();
        Map<Integer, Integer> yearlyCountMap = new TreeMap<>();

        for (Order order : orders) {
            if (order.getCreateTime() != null) {
                int yr = order.getCreateTime().getYear();
                yearlyMap.merge(yr, order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO, BigDecimal::add);
                yearlyCountMap.merge(yr, 1, Integer::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : yearlyMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("year", entry.getKey());
            item.put("totalAmount", entry.getValue());
            item.put("orderCount", yearlyCountMap.get(entry.getKey()));
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取分类商品数量统计")
    @GetMapping("/category-products")
    public Result<List<Map<String, Object>>> getCategoryProductCount(
            @Parameter(description = "店铺ID（可选，为空查全部）") @RequestParam(required = false) Long storeId) {
        List<Product> products;
        if (storeId != null) {
            // Get product IDs for this store from store_product table
            List<StoreProduct> storeProducts = storeProductService.getByStoreId(storeId);
            Set<Long> storeProductIds = storeProducts.stream()
                    .map(StoreProduct::getProductId)
                    .collect(Collectors.toSet());
            products = productService.list().stream()
                    .filter(p -> storeProductIds.contains(p.getId()))
                    .collect(Collectors.toList());
        } else {
            products = productService.list();
        }

        var categories = categoryService.list();
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        Map<Long, Integer> countMap = new LinkedHashMap<>();
        for (var cat : categories) {
            countMap.put(cat.getId(), 0);
        }

        for (Product product : products) {
            if (product.getCategoryId() != null) {
                countMap.merge(product.getCategoryId(), 1, Integer::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : countMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoryId", entry.getKey());
            item.put("categoryName", categoryNameMap.getOrDefault(entry.getKey(), "未知分类"));
            item.put("productCount", entry.getValue());
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取销量总额（总/店铺）")
    @GetMapping("/total")
    public Result<Map<String, Object>> getTotalSales(
            @Parameter(description = "店铺ID（可选，为空查全部）") @RequestParam(required = false) Long storeId) {
        List<Order> orders = getCompletedOrders(storeId);

        BigDecimal totalAmount = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("orderCount", orders.size());
        return Result.success(result);
    }

    private List<Order> getCompletedOrders(Long storeId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Order::getStatus, OrderStatus.PAID, OrderStatus.COMPLETED);
        if (storeId != null) {
            wrapper.eq(Order::getStoreId, storeId);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return orderService.list(wrapper);
    }

    @Operation(summary = "获取近7天销售趋势")
    @GetMapping("/trend/weekly")
    public Result<List<Map<String, Object>>> getWeeklySalesTrend(
            @Parameter(description = "店铺ID（可选）") @RequestParam(required = false) Long storeId) {
        List<Order> orders = getCompletedOrders(storeId);
        LocalDate today = LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal dayTotal = BigDecimal.ZERO;
            int dayCount = 0;
            for (Order order : orders) {
                if (order.getCreateTime() != null && order.getCreateTime().toLocalDate().equals(date)) {
                    dayTotal = dayTotal.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                    dayCount++;
                }
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("label", (date.getMonthValue()) + "/" + date.getDayOfMonth());
            item.put("totalAmount", dayTotal);
            item.put("orderCount", dayCount);
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取近6个月用户增长趋势")
    @GetMapping("/user-growth")
    public Result<List<Map<String, Object>>> getUserGrowth() {
        List<User> allUsers = userService.listAll();
        LocalDate today = LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate nextMonthStart = monthStart.plusMonths(1);
            int monthUsers = 0;
            int totalByMonth = 0;
            for (User user : allUsers) {
                if (user.getCreateTime() != null) {
                    LocalDate created = user.getCreateTime().toLocalDate();
                    if (!created.isBefore(monthStart) && created.isBefore(nextMonthStart)) {
                        monthUsers++;
                    }
                    if (created.isBefore(nextMonthStart)) {
                        totalByMonth++;
                    }
                }
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue()));
            item.put("label", monthStart.getMonthValue() + "月");
            item.put("newUsers", monthUsers);
            item.put("totalUsers", totalByMonth);
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取热门商品TOP10")
    @GetMapping("/top-products")
    public Result<List<Map<String, Object>>> getTopProducts(
            @Parameter(description = "店铺ID（可选）") @RequestParam(required = false) Long storeId) {
        List<Order> orders = getCompletedOrders(storeId);
        Map<Long, Product> productMap = productService.listAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        // Aggregate: productId -> {totalQty, totalAmount}
        Map<Long, int[]> productStats = new LinkedHashMap<>();
        for (Order order : orders) {
            if (order.getProductId() != null) {
                productStats.computeIfAbsent(order.getProductId(), k -> new int[]{0, 0});
                productStats.get(order.getProductId())[0] += order.getQuantity() != null ? order.getQuantity() : 0;
            }
        }

        // Also aggregate from order_items for multi-item orders
        List<OrderItem> allItems = orderItemService.listAll();
        Set<Long> completedOrderIds = orders.stream().map(Order::getId).collect(Collectors.toSet());
        for (OrderItem item : allItems) {
            if (completedOrderIds.contains(item.getOrderId()) && item.getProductId() != null) {
                productStats.computeIfAbsent(item.getProductId(), k -> new int[]{0, 0});
                productStats.get(item.getProductId())[0] += item.getQuantity() != null ? item.getQuantity() : 0;
            }
        }

        List<Map<String, Object>> result = productStats.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    Product p = productMap.get(entry.getKey());
                    item.put("productId", entry.getKey());
                    item.put("productName", p != null ? p.getName() : "商品#" + entry.getKey());
                    item.put("totalQuantity", entry.getValue()[0]);
                    item.put("price", p != null ? p.getPrice() : BigDecimal.ZERO);
                    return item;
                })
                .collect(Collectors.toList());

        return Result.success(result);
    }
}
