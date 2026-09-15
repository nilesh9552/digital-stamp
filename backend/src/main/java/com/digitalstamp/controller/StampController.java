package com.digitalstamp.controller;

import com.digitalstamp.dto.ApiResponse;
import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.stamp.ScanQrRequest;
import com.digitalstamp.dto.stamp.ScanResultResponse;
import com.digitalstamp.dto.stamp.StampActionRequest;
import com.digitalstamp.dto.stamp.StampTransactionResponse;
import com.digitalstamp.entity.User;
import com.digitalstamp.security.SecurityUtils;
import com.digitalstamp.service.ShopService;
import com.digitalstamp.service.StampService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stamps")
public class StampController {

    private final StampService stampService;
    private final ShopService shopService;

    public StampController(StampService stampService, ShopService shopService) {
        this.stampService = stampService;
        this.shopService = shopService;
    }

    @PostMapping("/scan")
    public ApiResponse<ScanResultResponse> scan(@Valid @RequestBody ScanQrRequest request) {
        return ApiResponse.ok(stampService.scan(SecurityUtils.currentUser(), request.getQrPayload()));
    }

    @PostMapping("/add")
    public ApiResponse<StampTransactionResponse> add(@Valid @RequestBody StampActionRequest request) {
        return ApiResponse.ok("Stamp added", stampService.addStamps(SecurityUtils.currentUser(), request));
    }

    @PostMapping("/reverse")
    public ApiResponse<StampTransactionResponse> reverse(@Valid @RequestBody StampActionRequest request) {
        return ApiResponse.ok("Stamp reversed", stampService.reverseStamps(SecurityUtils.currentUser(), request));
    }

    @GetMapping("/history")
    public ApiResponse<PagedResponse<StampTransactionResponse>> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = SecurityUtils.currentUser();
        var shop = shopService.requireOwnerShop(user);
        return ApiResponse.ok(stampService.historyForShop(shop.getId(), PageRequest.of(page, size)));
    }
}
