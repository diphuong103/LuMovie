package com.diph.lumovie.service;

import com.diph.lumovie.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    String createPaymentUrl(Long planId, Long userId, HttpServletRequest request);

    ApiResponse<String> processPaymentCallback(HttpServletRequest request);
}
