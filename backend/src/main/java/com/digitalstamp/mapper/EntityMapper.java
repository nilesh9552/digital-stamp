package com.digitalstamp.mapper;

import com.digitalstamp.dto.loyalty.LoyaltyCardResponse;
import com.digitalstamp.dto.reward.RedemptionResponse;
import com.digitalstamp.dto.reward.RewardResponse;
import com.digitalstamp.dto.shop.ShopResponse;
import com.digitalstamp.dto.stamp.StampTransactionResponse;
import com.digitalstamp.dto.user.UserResponse;
import com.digitalstamp.entity.LoyaltyCard;
import com.digitalstamp.entity.Reward;
import com.digitalstamp.entity.RewardRedemption;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.StampTransaction;
import com.digitalstamp.entity.User;
import com.digitalstamp.util.QrUtil;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public UserResponse toUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole())
                .active(user.isActive())
                .qrPayload(QrUtil.payload(user.getQrToken()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ShopResponse toShop(Shop shop, String programName, String programDesc, Integer required, Boolean programActive) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .slug(shop.getSlug())
                .logo(shop.getLogo())
                .description(shop.getDescription())
                .address(shop.getAddress())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .openingHours(shop.getOpeningHours())
                .active(shop.isActive())
                .ownerId(shop.getOwner() != null ? shop.getOwner().getId() : null)
                .ownerName(shop.getOwner() != null ? shop.getOwner().getName() : null)
                .ownerEmail(shop.getOwner() != null ? shop.getOwner().getEmail() : null)
                .loyaltyProgramName(programName)
                .loyaltyProgramDescription(programDesc)
                .requiredStamps(required)
                .loyaltyProgramActive(Boolean.TRUE.equals(programActive))
                .createdAt(shop.getCreatedAt())
                .build();
    }

    public LoyaltyCardResponse toCard(LoyaltyCard card) {
        int required = card.getLoyaltyProgram().getRequiredStamps();
        int remaining = Math.max(0, required - card.getCurrentStamps());
        return LoyaltyCardResponse.builder()
                .id(card.getId())
                .shopId(card.getShop().getId())
                .shopName(card.getShop().getName())
                .shopSlug(card.getShop().getSlug())
                .shopLogo(card.getShop().getLogo())
                .programId(card.getLoyaltyProgram().getId())
                .programName(card.getLoyaltyProgram().getName())
                .currentStamps(card.getCurrentStamps())
                .totalStamps(card.getTotalStamps())
                .requiredStamps(required)
                .stampsRemaining(remaining)
                .rewardEligible(card.getCurrentStamps() >= required && card.getLoyaltyProgram().isActive())
                .status(card.getStatus())
                .build();
    }

    public StampTransactionResponse toTx(StampTransaction tx) {
        return StampTransactionResponse.builder()
                .id(tx.getId())
                .loyaltyCardId(tx.getLoyaltyCard().getId())
                .customerId(tx.getCustomer().getId())
                .customerName(tx.getCustomer().getName())
                .shopId(tx.getShop().getId())
                .shopName(tx.getShop().getName())
                .stampsAdded(tx.getStampsAdded())
                .transactionType(tx.getTransactionType())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    public RewardResponse toReward(Reward reward, boolean eligible) {
        return RewardResponse.builder()
                .id(reward.getId())
                .shopId(reward.getShop().getId())
                .shopName(reward.getShop().getName())
                .loyaltyProgramId(reward.getLoyaltyProgram().getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .requiredStamps(reward.getRequiredStamps())
                .active(reward.isActive())
                .eligible(eligible)
                .build();
    }

    public RedemptionResponse toRedemption(RewardRedemption r) {
        return RedemptionResponse.builder()
                .id(r.getId())
                .rewardId(r.getReward().getId())
                .rewardName(r.getReward().getName())
                .customerId(r.getCustomer().getId())
                .customerName(r.getCustomer().getName())
                .shopId(r.getShop().getId())
                .shopName(r.getShop().getName())
                .loyaltyCardId(r.getLoyaltyCard().getId())
                .redeemedAt(r.getRedeemedAt())
                .status(r.getStatus())
                .redemptionCode(r.getRedemptionCode())
                .stampsConsumed(r.getStampsConsumed())
                .build();
    }
}
