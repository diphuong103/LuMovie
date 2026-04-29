package com.diph.lumovie.controller;

import com.diph.lumovie.dto.response.ApiResponse;
import com.diph.lumovie.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createPayment(@RequestParam Long planId,
            @RequestParam Long userId,
            HttpServletRequest request) {
        String url = paymentService.createPaymentUrl(planId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(url));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<String>> paymentCallback(HttpServletRequest request) {
        return ResponseEntity.ok(paymentService.processPaymentCallback(request));
    }
}
