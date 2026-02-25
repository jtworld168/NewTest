package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Payment;
import com.supermarket.enums.PaymentStatus;
import com.supermarket.service.PaymentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "支付管理", description = "订单支付增删改查接口")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "根据ID查询支付记录")
    @GetMapping("/get/{id}")
    public Result<Payment> getPaymentById(@Parameter(description = "支付ID") @PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return payment != null ? Result.success(payment) : Result.error("支付记录不存在");
    }

    @Operation(summary = "查询所有支付记录")
    @GetMapping("/list")
    public Result<List<Payment>> getAllPayments() {
        return Result.success(paymentService.listAll());
    }

    @Operation(summary = "根据订单ID查询支付记录")
    @GetMapping("/getByOrderId/{orderId}")
    public Result<Payment> getPaymentByOrderId(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return payment != null ? Result.success(payment) : Result.error("该订单无支付记录");
    }

    @Operation(summary = "根据支付状态查询支付记录")
    @GetMapping("/getByStatus/{status}")
    public Result<List<Payment>> getPaymentsByStatus(@Parameter(description = "支付状态") @PathVariable PaymentStatus status) {
        return Result.success(paymentService.getPaymentsByStatus(status));
    }

    @Operation(summary = "根据交易流水号查询支付记录")
    @GetMapping("/getByTransactionNo/{transactionNo}")
    public Result<Payment> getPaymentByTransactionNo(@Parameter(description = "交易流水号") @PathVariable String transactionNo) {
        Payment payment = paymentService.getPaymentByTransactionNo(transactionNo);
        return payment != null ? Result.success(payment) : Result.error("交易流水号不存在");
    }

    @Operation(summary = "模糊搜索支付记录（按交易流水号）")
    @GetMapping("/search")
    public Result<List<Payment>> searchPayments(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(paymentService.searchPayments(keyword));
    }

    @Operation(summary = "分页查询支付记录列表")
    @GetMapping("/listPage")
    public Result<IPage<Payment>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(paymentService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加支付记录")
    @PostMapping("/add")
    public Result<Void> addPayment(@RequestBody Payment payment) {
        if (payment.getOrderId() == null) {
            return Result.badRequest("订单ID不能为空");
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.badRequest("支付金额必须大于零");
        }
        if (payment.getPaymentMethod() == null) {
            return Result.badRequest("支付方式不能为空");
        }
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        return paymentService.addPayment(payment) ? Result.success() : Result.error("添加支付记录失败");
    }

    @Operation(summary = "更新支付记录")
    @PutMapping("/update")
    public Result<Void> updatePayment(@RequestBody Payment payment) {
        if (payment.getId() == null) {
            return Result.badRequest("支付ID不能为空");
        }
        if (paymentService.getPaymentById(payment.getId()) == null) {
            return Result.badRequest("支付记录不存在");
        }
        if (payment.getAmount() != null && payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.badRequest("支付金额必须大于零");
        }
        return paymentService.updatePayment(payment) ? Result.success() : Result.error("更新支付记录失败");
    }

    @Operation(summary = "删除支付记录")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deletePayment(@Parameter(description = "支付ID") @PathVariable Long id) {
        if (paymentService.getPaymentById(id) == null) {
            return Result.badRequest("支付记录不存在");
        }
        return paymentService.deletePayment(id) ? Result.success() : Result.error("删除支付记录失败");
    }

    @Operation(summary = "批量删除支付记录")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchPayments(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        List<Long> filteredIds = ids.stream().filter(id -> id != null).toList();
        if (filteredIds.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return paymentService.deleteBatchPayments(filteredIds) ? Result.success() : Result.error("批量删除支付记录失败");
    }
}
