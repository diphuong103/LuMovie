package com.diph.lumovie.service.impl;

import com.diph.lumovie.dto.response.ApiResponse;
import com.diph.lumovie.entity.SubscriptionPlan;
import com.diph.lumovie.entity.Transaction;
import com.diph.lumovie.entity.User;
import com.diph.lumovie.entity.UserSubscription;
import com.diph.lumovie.enums.SubscriptionStatus;
import com.diph.lumovie.enums.TransactionStatus;
import com.diph.lumovie.exception.ResourceNotFoundException;
import com.diph.lumovie.repository.SubscriptionPlanRepository;
import com.diph.lumovie.repository.TransactionRepository;
import com.diph.lumovie.repository.UserRepository;
import com.diph.lumovie.repository.UserSubscriptionRepository;
import com.diph.lumovie.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VnpayPaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Override
    @Transactional
    public String createPaymentUrl(Long planId, Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        String orderId = "VNPAY_" + UUID.randomUUID().toString().substring(0, 8);
        Transaction transaction = Transaction.builder()
                .user(user)
                .plan(plan)
                .amount(plan.getPrice())
                .orderId(orderId)
                .provider("VNPAY")
                .status(TransactionStatus.PENDING)
                .build();
        transactionRepository.save(transaction);

        // Mocking VNPAY URL return
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return baseUrl + "/api/payment/vnpay-return?vnp_TxnRef=" + orderId + "&vnp_ResponseCode=00";
    }

    @Override
    @Transactional
    public ApiResponse<String> processPaymentCallback(HttpServletRequest request) {
        String orderId = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        Transaction transaction = transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if ("00".equals(responseCode)) {
            transaction.setStatus(TransactionStatus.SUCCESS);

            User user = transaction.getUser();
            SubscriptionPlan plan = transaction.getPlan();

            UserSubscription subscription = UserSubscription.builder()
                    .user(user)
                    .plan(plan)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(plan.getDurationDays()))
                    .status(SubscriptionStatus.ACTIVE)
                    .build();
            userSubscriptionRepository.save(subscription);

            transactionRepository.save(transaction);
            return ApiResponse.ok("Payment successful, VIP activated!", "Success");
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return ApiResponse.error("Payment failed");
        }
    }
}
