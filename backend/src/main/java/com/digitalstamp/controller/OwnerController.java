package com.digitalstamp.controller;

import com.digitalstamp.dto.ApiResponse;
import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.admin.DashboardStatsResponse;
import com.digitalstamp.dto.loyalty.LoyaltyProgramRequest;
import com.digitalstamp.dto.reward.RedemptionResponse;
import com.digitalstamp.dto.reward.RewardRequest;
import com.digitalstamp.dto.reward.RewardResponse;
import com.digitalstamp.dto.shop.ShopRequest;
import com.digitalstamp.dto.shop.ShopResponse;
import com.digitalstamp.dto.user.OwnerCustomerRow;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.User;
import com.digitalstamp.security.SecurityUtils;
import com.digitalstamp.service.AdminService;
import com.digitalstamp.service.RewardService;
import com.digitalstamp.service.ShopService;
import com.digitalstamp.service.StampService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final AdminService adminService;
    private final ShopService shopService;
    private final StampService stampService;
    private final RewardService rewardService;

    public OwnerController(AdminService adminService,
                           ShopService shopService,
                           StampService stampService,
                           RewardService rewardService) {
        this.adminService = adminService;
        this.shopService = shopService;
        this.stampService = stampService;
        this.rewardService = rewardService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsResponse> dashboard() {
        return ApiResponse.ok(adminService.ownerDashboard(SecurityUtils.currentUser()));
    }

    @GetMapping("/shop")
    public ApiResponse<ShopResponse> shop() {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        return ApiResponse.ok(shopService.toResponse(shop));
    }

    @PutMapping("/shop")
    public ApiResponse<ShopResponse> updateShop(@Valid @RequestBody ShopRequest request) {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        request.setOwnerId(shop.getOwner() != null ? shop.getOwner().getId() : null);
        request.setSlug(shop.getSlug());
        request.setName(request.getName() != null ? request.getName() : shop.getName());
        return ApiResponse.ok(shopService.update(shop.getId(), request));
    }

    @GetMapping("/customers")
    public ApiResponse<PagedResponse<OwnerCustomerRow>> customers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        return ApiResponse.ok(stampService.shopCustomers(shop, q, PageRequest.of(page, size)));
    }

    @GetMapping("/loyalty")
    public ApiResponse<ShopResponse> loyalty() {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        return ApiResponse.ok(shopService.toResponse(shop));
    }

    @PutMapping("/loyalty")
    public ApiResponse<ShopResponse> updateLoyalty(@Valid @RequestBody LoyaltyProgramRequest request) {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        shopService.updateProgram(shop, request);
        return ApiResponse.ok(shopService.toResponse(shop));
    }

    @GetMapping("/rewards")
    public ApiResponse<List<RewardResponse>> rewards() {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        return ApiResponse.ok(rewardService.listForShop(shop));
    }

    @PostMapping("/rewards")
    public ApiResponse<RewardResponse> createReward(@Valid @RequestBody RewardRequest request) {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        return ApiResponse.ok("Reward created", rewardService.create(shop, request));
    }

    @PutMapping("/rewards/{id}")
    public ApiResponse<RewardResponse> updateReward(@PathVariable Long id, @Valid @RequestBody RewardRequest request) {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        return ApiResponse.ok(rewardService.update(shop, id, request));
    }

    @DeleteMapping("/rewards/{id}")
    public ApiResponse<Void> deleteReward(@PathVariable Long id) {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        rewardService.delete(shop, id);
        return ApiResponse.ok("Reward deleted", null);
    }

    @GetMapping("/redemptions")
    public ApiResponse<PagedResponse<RedemptionResponse>> redemptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Shop shop = shopService.requireOwnerShop(SecurityUtils.currentUser());
        return ApiResponse.ok(rewardService.shopHistory(shop.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/transactions")
    public ApiResponse<?> transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = SecurityUtils.currentUser();
        Shop shop = shopService.requireOwnerShop(user);
        return ApiResponse.ok(stampService.historyForShop(shop.getId(), PageRequest.of(page, size)));
    }
}
