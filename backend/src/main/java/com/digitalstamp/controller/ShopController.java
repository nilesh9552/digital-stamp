package com.digitalstamp.controller;

import com.digitalstamp.dto.ApiResponse;
import com.digitalstamp.dto.reward.RewardResponse;
import com.digitalstamp.dto.shop.ShopResponse;
import com.digitalstamp.entity.User;
import com.digitalstamp.security.SecurityUtils;
import com.digitalstamp.service.LoyaltyService;
import com.digitalstamp.service.RewardService;
import com.digitalstamp.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopService shopService;
    private final RewardService rewardService;
    private final LoyaltyService loyaltyService;

    public ShopController(ShopService shopService, RewardService rewardService, LoyaltyService loyaltyService) {
        this.shopService = shopService;
        this.rewardService = rewardService;
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/public")
    public ApiResponse<List<ShopResponse>> publicShops() {
        return ApiResponse.ok(shopService.publicShops());
    }

    @GetMapping("/{id}")
    public ApiResponse<ShopResponse> byId(@PathVariable Long id) {
        return ApiResponse.ok(shopService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<Map<String, Object>> bySlug(@PathVariable String slug) {
        ShopResponse shop = shopService.getBySlug(slug);
        List<RewardResponse> rewards = shop.getId() != null ? rewardService.publicForShop(shop.getId()) : List.of();
        return ApiResponse.ok(Map.of("shop", shop, "rewards", rewards));
    }

    @PostMapping("/slug/{slug}/enroll")
    public ApiResponse<?> enroll(@PathVariable String slug) {
        User user = SecurityUtils.currentUser();
        return ApiResponse.ok("Enrolled", loyaltyService.enrollCustomer(user, slug));
    }
}
