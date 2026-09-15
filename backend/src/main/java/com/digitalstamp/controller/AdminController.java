package com.digitalstamp.controller;

import com.digitalstamp.dto.ApiResponse;
import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.admin.CreateUserRequest;
import com.digitalstamp.dto.admin.DashboardStatsResponse;
import com.digitalstamp.dto.admin.ShopStatisticRow;
import com.digitalstamp.dto.reward.RedemptionResponse;
import com.digitalstamp.dto.shop.ShopRequest;
import com.digitalstamp.dto.shop.ShopResponse;
import com.digitalstamp.dto.stamp.StampTransactionResponse;
import com.digitalstamp.dto.user.UserResponse;
import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.SystemSetting;
import com.digitalstamp.service.AdminService;
import com.digitalstamp.service.RewardService;
import com.digitalstamp.service.ShopService;
import com.digitalstamp.service.StampService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ShopService shopService;
    private final StampService stampService;
    private final RewardService rewardService;

    public AdminController(AdminService adminService,
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
        return ApiResponse.ok(adminService.platformDashboard());
    }

    @GetMapping("/statistics")
    public ApiResponse<List<ShopStatisticRow>> statistics() {
        return ApiResponse.ok(adminService.shopStatistics());
    }

    @GetMapping("/shops")
    public ApiResponse<PagedResponse<ShopResponse>> shops(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(shopService.adminList(q, PageRequest.of(page, size)));
    }

    @PostMapping("/shops")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShopResponse> createShop(@Valid @RequestBody ShopRequest request) {
        return ApiResponse.ok("Shop created", shopService.create(request));
    }

    @PutMapping("/shops/{id}")
    public ApiResponse<ShopResponse> updateShop(@PathVariable Long id, @Valid @RequestBody ShopRequest request) {
        return ApiResponse.ok(shopService.update(id, request));
    }

    @PutMapping("/shops/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        shopService.setActive(id, true);
        return ApiResponse.ok("Shop activated", null);
    }

    @PutMapping("/shops/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        shopService.setActive(id, false);
        return ApiResponse.ok("Shop deactivated", null);
    }

    @DeleteMapping("/shops/{id}")
    public ApiResponse<Void> deleteShop(@PathVariable Long id) {
        shopService.delete(id);
        return ApiResponse.ok("Shop deleted", null);
    }

    @GetMapping("/shop-owners")
    public ApiResponse<PagedResponse<UserResponse>> owners(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminService.usersByRole(Role.SHOP_OWNER, q, PageRequest.of(page, size)));
    }

    @PostMapping("/shop-owners")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createOwner(@Valid @RequestBody CreateUserRequest request) {
        request.setRole(Role.SHOP_OWNER);
        return ApiResponse.ok("Shop owner created", adminService.createUser(request));
    }

    @PutMapping("/shop-owners/{id}")
    public ApiResponse<UserResponse> updateOwner(@PathVariable Long id, @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(adminService.updateUser(id, request));
    }

    @GetMapping("/customers")
    public ApiResponse<PagedResponse<UserResponse>> customers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminService.usersByRole(Role.CUSTOMER, q, PageRequest.of(page, size)));
    }

    @PutMapping("/customers/{id}")
    public ApiResponse<UserResponse> updateCustomer(@PathVariable Long id, @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(adminService.updateUser(id, request));
    }

    @GetMapping("/transactions")
    public ApiResponse<PagedResponse<StampTransactionResponse>> transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stampService.historyAll(PageRequest.of(page, size)));
    }

    @GetMapping("/redemptions")
    public ApiResponse<PagedResponse<RedemptionResponse>> redemptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(rewardService.allHistory(PageRequest.of(page, size)));
    }

    @GetMapping("/settings")
    public ApiResponse<List<SystemSetting>> settings() {
        return ApiResponse.ok(adminService.settings());
    }

    @PutMapping("/settings")
    public ApiResponse<SystemSetting> upsertSetting(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(adminService.upsertSetting(body.get("key"), body.get("value"), body.get("description")));
    }
}
