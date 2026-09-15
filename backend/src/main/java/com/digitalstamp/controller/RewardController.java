package com.digitalstamp.controller;

import com.digitalstamp.dto.ApiResponse;
import com.digitalstamp.dto.reward.RedemptionResponse;
import com.digitalstamp.entity.User;
import com.digitalstamp.security.SecurityUtils;
import com.digitalstamp.service.RewardService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping("/{id}/redeem")
    public ApiResponse<RedemptionResponse> redeem(@PathVariable Long id) {
        User user = SecurityUtils.currentUser();
        return ApiResponse.ok("Reward redeemed", rewardService.redeem(user, id));
    }
}
