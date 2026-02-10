package com.supermarket.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.supermarket.common.Result;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.service.ProductService;
import com.supermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
}
