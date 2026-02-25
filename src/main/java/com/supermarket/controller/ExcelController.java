package com.supermarket.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.supermarket.common.Result;
import com.supermarket.entity.Order;
import com.supermarket.entity.Product;
import com.supermarket.entity.Store;
import com.supermarket.entity.StoreProduct;
import com.supermarket.entity.StoreProductExcelDTO;
import com.supermarket.entity.User;
import com.supermarket.service.OrderService;
import com.supermarket.service.ProductService;
import com.supermarket.service.StoreService;
import com.supermarket.service.StoreProductService;
import com.supermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Excel导入导出", description = "EasyExcel数据导入导出接口")
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final StoreService storeService;
    private final StoreProductService storeProductService;

    @Operation(summary = "导出商品数据")
    @GetMapping("/export/products")
    public void exportProducts(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("商品数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        List<Product> products = productService.listAll();
        EasyExcel.write(response.getOutputStream(), Product.class).sheet("商品").doWrite(products);
    }

    @Operation(summary = "导入商品数据")
    @PostMapping("/import/products")
    public Result<String> importProducts(@RequestParam("file") MultipartFile file) throws IOException {
        List<Product> products = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), Product.class, new PageReadListener<Product>(products::addAll))
                .sheet().doRead();
        for (Product p : products) {
            p.setId(null);
            productService.addProduct(p);
        }
        return Result.success("成功导入 " + products.size() + " 条商品数据");
    }

    @Operation(summary = "导出用户数据")
    @GetMapping("/export/users")
    public void exportUsers(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        List<User> users = userService.listAll();
        EasyExcel.write(response.getOutputStream(), User.class).sheet("用户").doWrite(users);
    }

    @Operation(summary = "导入用户数据")
    @PostMapping("/import/users")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        List<User> users = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), User.class, new PageReadListener<User>(users::addAll))
                .sheet().doRead();
        for (User u : users) {
            u.setId(null);
            userService.addUser(u);
        }
        return Result.success("成功导入 " + users.size() + " 条用户数据");
    }

    @Operation(summary = "导出订单数据")
    @GetMapping("/export/orders")
    public void exportOrders(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("订单数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        List<Order> orders = orderService.listAll();
        EasyExcel.write(response.getOutputStream(), Order.class).sheet("订单").doWrite(orders);
    }

    @Operation(summary = "导出店铺数据")
    @GetMapping("/export/stores")
    public void exportStores(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("店铺数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        List<Store> stores = storeService.listAll();
        EasyExcel.write(response.getOutputStream(), Store.class).sheet("店铺").doWrite(stores);
    }

    @Operation(summary = "导出店铺商品数据（支持筛选）")
    @GetMapping("/export/store-products")
    public void exportStoreProducts(
            HttpServletResponse response,
            @Parameter(description = "店铺ID（可选）") @RequestParam(required = false) Long storeId,
            @Parameter(description = "商品名称搜索（可选）") @RequestParam(required = false) String productName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("店铺商品数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        List<StoreProduct> storeProducts;
        if (productName != null && !productName.isBlank()) {
            storeProducts = storeProductService.searchByProductNameList(productName.trim(), storeId);
        } else if (storeId != null) {
            storeProducts = storeProductService.getByStoreId(storeId);
        } else {
            storeProducts = storeProductService.listAll();
        }

        List<StoreProductExcelDTO> dtoList = new ArrayList<>();
        for (StoreProduct sp : storeProducts) {
            StoreProductExcelDTO dto = new StoreProductExcelDTO();
            dto.setStoreId(sp.getStoreId());
            dto.setStorePrice(sp.getStorePrice());
            dto.setStoreStock(sp.getStoreStock());
            dto.setSafetyStock(sp.getSafetyStock());
            dto.setStatus(sp.getStatus());
            Product product = productService.getProductById(sp.getProductId());
            if (product != null) {
                dto.setBarcode(product.getBarcode());
                dto.setProductName(product.getName());
            }
            dtoList.add(dto);
        }
        EasyExcel.write(response.getOutputStream(), StoreProductExcelDTO.class).sheet("店铺商品").doWrite(dtoList);
    }

    @Operation(summary = "导入店铺商品数据（相同条码合并库存）")
    @PostMapping("/import/store-products")
    public Result<String> importStoreProducts(@RequestParam("file") MultipartFile file) throws IOException {
        List<StoreProductExcelDTO> dtoList = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), StoreProductExcelDTO.class, new PageReadListener<StoreProductExcelDTO>(dtoList::addAll))
                .sheet().doRead();
        int imported = 0;
        int merged = 0;
        for (StoreProductExcelDTO dto : dtoList) {
            if (dto.getStoreId() == null || (dto.getBarcode() == null && dto.getProductName() == null)) {
                continue;
            }
            Product product = null;
            if (dto.getBarcode() != null && !dto.getBarcode().isBlank()) {
                product = productService.getProductByBarcode(dto.getBarcode().trim());
            }
            if (product == null && dto.getProductName() != null && !dto.getProductName().isBlank()) {
                product = productService.getProductByName(dto.getProductName().trim());
            }
            if (product == null) {
                Product newProduct = new Product();
                newProduct.setName(dto.getProductName() != null ? dto.getProductName().trim() : dto.getBarcode().trim());
                newProduct.setBarcode(dto.getBarcode() != null ? dto.getBarcode().trim() : null);
                newProduct.setPrice(dto.getStorePrice() != null ? dto.getStorePrice() : java.math.BigDecimal.ZERO);
                newProduct.setStock(0);
                newProduct.setStatus(1);
                productService.addProduct(newProduct);
                product = newProduct;
            }

            StoreProduct existing = storeProductService.getByStoreIdAndProductId(dto.getStoreId(), product.getId());
            if (existing != null) {
                int addedStock = dto.getStoreStock() != null ? dto.getStoreStock() : 0;
                existing.setStoreStock((existing.getStoreStock() != null ? existing.getStoreStock() : 0) + addedStock);
                if (dto.getStorePrice() != null) {
                    existing.setStorePrice(dto.getStorePrice());
                }
                if (dto.getSafetyStock() != null) {
                    existing.setSafetyStock(dto.getSafetyStock());
                }
                if (dto.getStatus() != null) {
                    existing.setStatus(dto.getStatus());
                }
                storeProductService.updateStoreProduct(existing);
                merged++;
            } else {
                StoreProduct sp = new StoreProduct();
                sp.setStoreId(dto.getStoreId());
                sp.setProductId(product.getId());
                sp.setStorePrice(dto.getStorePrice());
                sp.setStoreStock(dto.getStoreStock() != null ? dto.getStoreStock() : 0);
                sp.setSafetyStock(dto.getSafetyStock() != null ? dto.getSafetyStock() : 10);
                sp.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
                storeProductService.addStoreProduct(sp);
                imported++;
            }
            storeProductService.syncProductTotalStock(product.getId());
        }
        return Result.success("成功导入 " + imported + " 条，合并 " + merged + " 条店铺商品数据");
    }
}
