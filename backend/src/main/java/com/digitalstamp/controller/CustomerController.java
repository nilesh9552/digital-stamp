package com.digitalstamp.controller;

import com.digitalstamp.dto.ApiResponse;
import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.loyalty.LoyaltyCardResponse;
import com.digitalstamp.dto.reward.RedemptionResponse;
import com.digitalstamp.dto.reward.RewardResponse;
import com.digitalstamp.dto.stamp.StampTransactionResponse;
import com.digitalstamp.dto.user.UpdateProfileRequest;
import com.digitalstamp.dto.user.UserResponse;
import com.digitalstamp.entity.User;
import com.digitalstamp.mapper.EntityMapper;
import com.digitalstamp.security.SecurityUtils;
import com.digitalstamp.service.AdminService;
import com.digitalstamp.service.LoyaltyService;
import com.digitalstamp.service.RewardService;
import com.digitalstamp.service.StampService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final EntityMapper mapper;
    private final AdminService adminService;
    private final LoyaltyService loyaltyService;
    private final RewardService rewardService;
    private final StampService stampService;

    public CustomerController(EntityMapper mapper,
                              AdminService adminService,
                              LoyaltyService loyaltyService,
                              RewardService rewardService,
                              StampService stampService) {
        this.mapper = mapper;
        this.adminService = adminService;
        this.loyaltyService = loyaltyService;
        this.rewardService = rewardService;
        this.stampService = stampService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> profile() {
        return ApiResponse.ok(mapper.toUser(SecurityUtils.currentUser()));
    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> update(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(adminService.updateProfile(SecurityUtils.currentUser(), request));
    }

    @GetMapping("/qr")
    public ApiResponse<Map<String, String>> qr() {
        User user = SecurityUtils.currentUser();
        return ApiResponse.ok(Map.of("qrPayload", com.digitalstamp.util.QrUtil.payload(user.getQrToken())));
    }

    @GetMapping("/loyalty-cards")
    public ApiResponse<List<LoyaltyCardResponse>> cards() {
        return ApiResponse.ok(loyaltyService.cardsFor(SecurityUtils.currentUser()));
    }

    @GetMapping("/shops")
    public ApiResponse<List<LoyaltyCardResponse>> shops() {
        return ApiResponse.ok(loyaltyService.cardsFor(SecurityUtils.currentUser()));
    }

    @GetMapping("/rewards")
    public ApiResponse<List<RewardResponse>> rewards() {
        return ApiResponse.ok(rewardService.customerRewards(SecurityUtils.currentUser()));
    }

    @GetMapping("/history")
    public ApiResponse<PagedResponse<StampTransactionResponse>> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stampService.historyForCustomer(SecurityUtils.currentUser().getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/redemptions")
    public ApiResponse<PagedResponse<RedemptionResponse>> redemptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(rewardService.customerHistory(SecurityUtils.currentUser().getId(), PageRequest.of(page, size)));
    }
}
